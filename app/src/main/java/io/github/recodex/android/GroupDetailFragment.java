package io.github.recodex.android;

import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.AssignmentSolution;
import io.github.recodex.android.model.AssignmentSolutionSubmission;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.LocalizedAssignment;
import io.github.recodex.android.model.LocalizedGroup;
import io.github.recodex.android.users.UsersManager;
import io.github.recodex.android.utils.LocalizationHelper;
import retrofit2.Response;


/**
 * Displays details of a group
 */
public class GroupDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_GROUP_ID = "groupId";

    @Inject
    ApiWrapper<RecodexApi> api;

    @Inject
    UsersManager users;

    @Inject
    LocalizationHelper localizationHelper;

    private String groupId;

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
            new AsyncTask<Void, Void, GroupData>() {
                @Override
                protected GroupData doInBackground(Void... params) {
                    return fetchGroupData(api.fromRemote());
                }

                @Override
                protected void onPostExecute(GroupData data) {
                    if (data != null) {
                        renderData(data.group, data.assignments);
                    } else {
                        Toast.makeText(getContext(), R.string.loading_group_detail_failed, Toast.LENGTH_SHORT).show();
                    }

                    swipeLayout.setRefreshing(false);
                }
            }.execute();
        }
    }

    class GroupData {
        public Group group;

        List<AssignmentData> assignments;
    }

    class AssignmentData {
        Assignment assignment;
        AssignmentSolution bestSolution;
        AssignmentSolutionSubmission bestSolutionSubmission;

        AssignmentData(Assignment assignment, AssignmentSolution bestSolution) {
            this.assignment = assignment;
            this.bestSolution = bestSolution;
            this.bestSolutionSubmission = bestSolution != null ? bestSolution.getLastSubmission(): null;
        }

        boolean hasEvaluation() {
            return bestSolutionSubmission != null && bestSolutionSubmission.getEvaluation() != null;
        }

        int getPointPercentage() {
            final int pointsGained = bestSolutionSubmission.getEvaluation().getPoints() + bestSolutionSubmission.getEvaluation().getBonusPoints();
            return (100 * pointsGained) / bestSolution.getMaxPoints();
        }

        boolean isAfterDeadlines(Date now) {
            return isAfterFirstDeadline(now) && isAfterSecondDeadline(now);
        }

        boolean isAfterFirstDeadline(Date now) {
            return assignment.getFirstDeadline() * 1000 < now.getTime();
        }

        boolean isAfterSecondDeadline(Date now) {
            return assignment.isAllowedSecondDeadline()
                    ? assignment.getSecondDeadline() * 1000 < now.getTime()
                    : isAfterFirstDeadline(now);
        }
    }

    private GroupData fetchGroupData(RecodexApi api) {
        try {
            Response<Envelope<Group>> groupResponse = api.getGroup(groupId).execute();

            if (checkApiResponse(groupResponse)) {
                GroupData result = new GroupData();
                result.group = groupResponse.body().getPayload();
                result.assignments = new ArrayList<>();

                for (String assignmentId : result.group.getPrivateData().getAssignments()) {
                    Response<Envelope<Assignment>> assignmentResponse = api.getAssignment(assignmentId).execute();

                    if (!checkApiResponse(assignmentResponse)) {
                        continue;
                    }

                    Response<Envelope<AssignmentSolution>> solutionResponse = api.getBestAssignmentSolution(assignmentId, users.getCurrentUser().getId()).execute();
                    AssignmentSolution bestSolution = null;
                    if (checkApiResponse(solutionResponse)) {
                        bestSolution = solutionResponse.body().getPayload();
                    }

                    result.assignments.add(new AssignmentData(
                            assignmentResponse.body().getPayload(),
                            bestSolution
                    ));
                }

                return result;
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    private void renderData(Group group, List<AssignmentData> assignments) {
        if (getView() == null) {
            return;
        }

        LocalizedGroup localizedGroup = localizationHelper.getUserLocalizedText(group.getLocalizedTexts());
        ((TextView) getView().findViewById(R.id.group_description))
                .setText(localizedGroup != null ? localizedGroup.getDescription() : "");
        getActivity().setTitle(localizedGroup != null ? localizedGroup.getName() : "");

        ((ListView) getView().findViewById(R.id.group_assignments)).setAdapter(new AssignmentListAdapter(getContext(), assignments));
    }

    private <T> boolean checkApiResponse(Response<Envelope<T>> response) {
        return response.isSuccessful() && response.body().isSuccess();
    }

    class AssignmentListAdapter extends ArrayAdapter<AssignmentData> {
        private List<AssignmentData> assignments;

        private LayoutInflater inflater;

        AssignmentListAdapter(Context context, List<AssignmentData> assignments) {
            super(context, R.layout.fragment_group_detail);
            this.inflater = LayoutInflater.from(context);
            this.assignments = assignments;
            addAll(assignments);

            Collections.sort(assignments, new Comparator<AssignmentData>() {
                private Date now = Calendar.getInstance().getTime();

                @Override
                public int compare(AssignmentData first, AssignmentData second) {
                    boolean firstExpired = first.isAfterDeadlines(now);
                    boolean secondExpired = second.isAfterDeadlines(now);

                    boolean firstDone = first.hasEvaluation() && first.getPointPercentage() >= 100;
                    boolean secondDone = second.hasEvaluation() && second.getPointPercentage() >= 100;

                    boolean firstToDo = !firstDone && !firstExpired;
                    boolean secondToDo = !secondDone && !secondExpired;

                    // Unfinished assignments go first
                    if (firstToDo && !secondToDo) {
                        return -1;
                    }
                    if (!firstToDo && secondToDo) {
                        return 1;
                    }

                    // Missed assignments (no points and after deadline) go last
                    boolean firstMissed = firstExpired && !firstDone;
                    boolean secondMissed = secondExpired && !secondDone;

                    if (!firstMissed && secondMissed) {
                        return -1;
                    }
                    if (firstMissed && !secondMissed) {
                        return 1;
                    }

                    // Missed assignments are sorted by creation date
                    if (firstMissed && secondMissed) {
                        return 0; // TODO missing in the response
                    }

                    // Equal items get sorted by earliest deadline
                    long firstAssignmentDeadline = first.isAfterFirstDeadline(now)
                            ? first.assignment.getSecondDeadline()
                            : first.assignment.getFirstDeadline();
                    long secondAssignmentDeadline = second.isAfterFirstDeadline(now)
                            ? second.assignment.getSecondDeadline()
                            : second.assignment.getFirstDeadline();

                    if (firstAssignmentDeadline > secondAssignmentDeadline) {
                        return 1;
                    }
                    if (firstAssignmentDeadline < secondAssignmentDeadline) {
                        return -1;
                    }

                    return 0;
                }
            });
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view;
            Date now = Calendar.getInstance().getTime();

            if (convertView == null) {
                view = inflater.inflate(R.layout.assignment_list_item, parent, false);
            } else {
                view = convertView;
            }

            final AssignmentData data = assignments.get(position);
            final Assignment assignment = data.assignment;
            final LocalizedAssignment localizedAssignment = localizationHelper.getUserLocalizedText(assignment.getLocalizedTexts());
            final boolean assignmentDone = data.hasEvaluation() && data.getPointPercentage() >= 100;

            TextView name = (TextView) view.findViewById(R.id.assignment_name);
            name.setText(localizedAssignment != null ? localizedAssignment.getName() : "");
            if (assignmentDone) {
                name.setTextColor(getResources().getColor(R.color.colorAssignmentDone));
            } else if (data.isAfterDeadlines(now)) {
                name.setTextColor(getResources().getColor(R.color.colorAssignmentMissed));
            }

            String firstDeadline = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                    .format(new Date(assignment.getFirstDeadline() * 1000));
            TextView firstDeadlineText = (TextView) view.findViewById(R.id.deadline1_text);
            firstDeadlineText.setText(firstDeadline);
            if (data.isAfterFirstDeadline(now)) {
                firstDeadlineText.setPaintFlags(firstDeadlineText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                firstDeadlineText.setPaintFlags(firstDeadlineText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (assignment.isAllowedSecondDeadline()) {
                String secondDeadline = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                        .format(new Date(assignment.getSecondDeadline() * 1000));
                TextView secondDeadlineText = (TextView) view.findViewById(R.id.deadline2_text);
                secondDeadlineText.setText(secondDeadline);
                if (data.isAfterSecondDeadline(now)) {
                    secondDeadlineText.setPaintFlags(secondDeadlineText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    secondDeadlineText.setPaintFlags(secondDeadlineText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

            TextView percentage = (TextView) view.findViewById(R.id.solutions_button);
            if (data.hasEvaluation()) {
                if (assignmentDone) {
                    percentage.setTextColor(getResources().getColor(R.color.colorAssignmentDone));
                }
                percentage.setText(String.format(Locale.ROOT, "%d%%", data.getPointPercentage()));
            } else {
                percentage.setText("0%");
            }

            view.findViewById(R.id.assignment_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textCallback != null) {
                        textCallback.onAssignmentTextSelected(assignment.getId());
                    }
                }
            });

            view.findViewById(R.id.solutions_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (solutionsCallback != null) {
                        solutionsCallback.onAssignmentSolutionsSelected(assignment.getId());
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_group_detail, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        swipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark);
        swipeLayout.setNestedScrollingEnabled(true);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Try to load data from the cache so that there is something to display
        new AsyncTask<Void, Void, GroupData>() {
            @Override
            protected GroupData doInBackground(Void... params) {
                return fetchGroupData(api.fromCache());
            }

            @Override
            protected void onPostExecute(GroupData data) {
                if (data != null) {
                    renderData(data.group, data.assignments);
                } else {
                    startForcedReload();
                }
            }
        }.execute();
    }

    private void startForcedReload() {
        swipeLayout.setRefreshing(true);
        onRefresh();
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
