package io.github.recodex.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.StudentGroupStats;
import io.github.recodex.android.users.UserWrapper;
import io.github.recodex.android.users.UsersManager;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class GroupListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    UsersManager users;

    private ListFragment fragment = this;
    private OnGroupSelectedListener callback;
    private SwipeRefreshLayout swipeLayout = null;

    class GroupListAdapter extends ArrayAdapter<Group> {
        private final List<StudentGroupStats> statsList;
        private List<Group> groups;

        private LayoutInflater inflater;

        GroupListAdapter(Context context, List<Group> groups, List<StudentGroupStats> stats) {
            super(context, R.layout.fragment_group_list);
            this.groups = groups;
            this.statsList = stats;
            this.inflater = LayoutInflater.from(context);
            addAll(groups);
        }

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.group_list_item, parent, false);
            } else {
                view = convertView;
            }

            final Group group = groups.get(position);
            StudentGroupStats stats = null;

            for (StudentGroupStats statsItem : statsList) {
                if (statsItem.getGroupId().equals(group.getId())) {
                    stats = statsItem;
                    break;
                }
            }

            TextView groupName = (TextView) view.findViewById(R.id.group_name);
            groupName.setText(group.getName());

            ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
            progress.setMax(stats.getTotalPoints());
            progress.setProgress(stats.getGainedPoints());

            TextView percent = (TextView) view.findViewById(R.id.percent);
            int points_percent = stats.getGainedPoints() * 100 /
                    (stats.getTotalPoints() == 0 ? 1 : stats.getTotalPoints());
            percent.setText(String.format(Locale.ROOT, "%d%%", points_percent));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onGroupSelected(group.getId());
                    }
                }
            });
            return view;
        }
    }

    public GroupListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);

        setListAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_activity_navigation_drawer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.fragment_group_list, container, false);
        swipeLayout = (SwipeRefreshLayout) parent.findViewById(R.id.swipe_container);
        swipeLayout.addView(v);

        swipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark);
        swipeLayout.setNestedScrollingEnabled(true);
        swipeLayout.setOnRefreshListener(this);

        fillData();
        onRefresh();
        return parent;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragment.setListShown(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnGroupSelectedListener) context;
    }

    @Override
    public void onRefresh() {
        if (users.getCurrentUser() != null) {
            fillData();
        }
    }

    public void fillData() {
        UserWrapper currentUser = users.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        List<Group> groups = currentUser.getGroups();
        List<StudentGroupStats> stats = currentUser.getGroupStats();
        if (groups == null || stats == null) {
            return;
        }

        ArrayAdapter<Group> adapter = new GroupListAdapter(fragment.getContext(), groups, stats);
        fragment.setListAdapter(adapter);
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupId);
    }
}
