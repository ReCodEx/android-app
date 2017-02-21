package io.github.recodex.android;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.StudentGroupStats;
import io.github.recodex.android.model.UserGroups;
import io.github.recodex.android.users.UsersManager;
import retrofit2.Response;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class GroupListFragment extends ListFragment {
    @Inject
    RecodexApi api;

    @Inject
    UsersManager users;

    private ListFragment fragment = this;
    private ArrayAdapter<Group> adapter;

    private OnGroupSelectedListener callback;

    class LoadGroupsTask extends AsyncTask<Void, Void, UserGroups> {
        protected UserGroups doInBackground(Void... params) {
            try {
                Response<Envelope<UserGroups>> response = api.getGroupsForUser(users.getCurrentUser().getId()).execute();

                if (!response.isSuccessful()) {
                    return null;
                }

                Envelope<UserGroups> body = response.body();
                return body.getPayload();
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(UserGroups groups) {

            if (groups == null) {
                // loading failed
                adapter = new ArrayAdapter<>(fragment.getContext(), R.layout.fragment_group_list);
                Toast.makeText(fragment.getContext(), R.string.loadingFailed, Toast.LENGTH_LONG).show();
            } else {
                // we got the actual data
                adapter = new GroupListAdapter(fragment.getContext(), groups.getStudent(), groups.getStats());
            }
        }
    }

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
            int points_percent = stats.getGainedPoints() * 100 / stats.getTotalPoints();
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

        new LoadGroupsTask().execute();
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
        parent.addView(v, 0);
        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fragment.setListAdapter(adapter);
        fragment.setListShown(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnGroupSelectedListener) context;
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupId);
    }
}
