package io.github.recodex.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.users.UsersManager;
import retrofit2.Response;


/**
 * Displays details of a group
 */
public class GroupDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_GROUP_ID = "groupId";

    @Inject
    RecodexApi api;

    @Inject
    UsersManager users;

    private String groupId;

    private GroupDetailFragment fragment = this;

    private SwipeRefreshLayout swipeLayout = null;

    /**
     * Called when the assignment text should be displayed
     */
    private OnAssignmentTextSelectedListener textCallback = null;

    /**
     * Called when the solutions submitted to an assignment should be displayed
     */
    private OnAssignmentSolutionsSelectedListener solutionsCallback = null;

    @Override
    public void onRefresh() {
        if (users.getCurrentUser() != null) {
            new LoadGroupTask().execute();
        }
    }

    class GroupData {
        public Group group;

        public List<Assignment> assignments;
    }

    class LoadGroupTask extends AsyncTask<Void, Void, GroupData> {
        protected GroupData doInBackground(Void... params) {
            try {
                GroupData result = new GroupData();

                result.group = users.getCurrentUser().getGroup(groupId);
                result.assignments = new ArrayList<>();

                for (String assignmentId : result.group.getAssignments().getPublic()) {
                    Response<Envelope<Assignment>> assignmentResponse = api.getAssignment(assignmentId).execute();
                    if (assignmentResponse.isSuccessful()) {
                        result.assignments.add(assignmentResponse.body().getPayload());
                    }
                }

                return result;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(GroupData data) {
            if (data == null) {
                Toast.makeText(fragment.getContext(), R.string.loading_group_detail_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            ((TextView) fragment.getView().findViewById(R.id.group_description)).setText(data.group.getDescription());
            getActivity().setTitle(data.group.getName());

            ((ListView) fragment.getView().findViewById(R.id.group_assignments)).setAdapter(new AssignmentListAdapter(getContext(), data.assignments));
            swipeLayout.setRefreshing(false);
        }
    }

    class AssignmentListAdapter extends ArrayAdapter<Assignment> {
        private List<Assignment> assignments;

        private LayoutInflater inflater;

        AssignmentListAdapter(Context context, List<Assignment> assignments) {
            super(context, R.layout.fragment_group_detail);
            this.assignments = assignments;
            this.inflater = LayoutInflater.from(context);
            addAll(assignments);
        }

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.assignment_list_item, parent, false);
            } else {
                view = convertView;
            }

            ((TextView) view.findViewById(R.id.assignment_name))
                    .setText(assignments.get(position).getName());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (solutionsCallback != null) {
                        solutionsCallback.onAssignmentSolutionsSelected(assignments.get(position).getId());
                    }
                }
            });

            view.findViewById(R.id.assignmentInfoIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textCallback != null) {
                        textCallback.onAssignmentTextSelected(assignments.get(position).getId());
                    }
                }
            });

            return view;
        }
    }

    public GroupDetailFragment() {
        // Required empty public constructor
    }

    public static GroupDetailFragment newInstance(String groupId) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);

        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }

        new LoadGroupTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_group_detail, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        swipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark);
        swipeLayout.setNestedScrollingEnabled(true);
        swipeLayout.setOnRefreshListener(this);

        onRefresh();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAssignmentTextSelectedListener && context instanceof OnAssignmentSolutionsSelectedListener) {
            solutionsCallback = (OnAssignmentSolutionsSelectedListener) context;
            textCallback = (OnAssignmentTextSelectedListener) context;
        }
    }

    public interface OnAssignmentTextSelectedListener {
        void onAssignmentTextSelected(String assignmentId);
    }

    public interface OnAssignmentSolutionsSelectedListener {
        void onAssignmentSolutionsSelected(String assignmentId);
    }
}
