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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

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

    ListFragment fragment = this;

    private GroupListAdapter adapter;

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
                // TODO loading failed - do something smart
                return;
            }

            adapter = new GroupListAdapter(fragment.getContext(), groups.getStudent(), groups.getStats());

            fragment.setListAdapter(adapter);
            fragment.setListShown(true);
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

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "clicked", Toast.LENGTH_LONG).show();
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

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);

        setListAdapter(null);

        new LoadGroupsTask().execute();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.fragment_group_list, container, false);
        parent.addView(v, 0);
        return parent;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnGroupSelectedListener) context;
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupId);
    }
}
