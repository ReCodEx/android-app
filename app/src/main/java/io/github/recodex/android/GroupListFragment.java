package io.github.recodex.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.LocalizedGroup;
import io.github.recodex.android.model.StudentGroupStats;
import io.github.recodex.android.model.UserGroups;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.users.UserWrapper;
import io.github.recodex.android.users.UsersManager;
import io.github.recodex.android.utils.LocalizationHelper;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class GroupListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    UsersManager users;

    @Inject
    LocalizationHelper localizationHelper;

    @Inject
    ApiDataFetcher userDataFetcher;

    private final ListFragment fragment = this;
    private OnGroupSelectedListener callback;
    private SwipeRefreshLayout swipeLayout = null;

    class LoadGroupsTask extends AsyncTask<Void, Void, UserGroups> {
        protected UserGroups doInBackground(Void... params) {
            try {
                return userDataFetcher.fetchAndStoreGroups(users.getCurrentUser());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserGroups groups) {
            if (groups == null) {
                // loading failed
                Toast.makeText(fragment.getContext(), R.string.loading_group_list_failed, Toast.LENGTH_SHORT).show();
            } else {
                users.getCurrentUser().setGroupsInfo(groups.getStudent(), groups.getStats());
            }

            fillData();
            swipeLayout.setRefreshing(false);
        }
    }

    class GroupListAdapter extends ArrayAdapter<Group> {
        private final List<StudentGroupStats> statsList;
        private final List<Group> groups;
        private final LayoutInflater inflater;

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
                if (TextUtils.equals(statsItem.getGroupId(), group.getId())) {
                    stats = statsItem;
                    break;
                }
            }

            // stats not found, cannot proceed further
            if (stats == null) {
                return view;
            }

            LocalizedGroup localizedGroup = localizationHelper.getUserLocalizedText(group.getLocalizedTexts());
            TextView groupName = (TextView) view.findViewById(R.id.group_name);
            groupName.setText(localizedGroup != null ? localizedGroup.getName() : "");

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragment.setListShown(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (OnGroupSelectedListener) context;
    }

    @Override
    public void onRefresh() {
        if (users.getCurrentUser() != null) {
            new LoadGroupsTask().execute();
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
