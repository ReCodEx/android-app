package io.github.recodex.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.AssignmentSolution;
import io.github.recodex.android.model.AssignmentSolutionSubmission;
import io.github.recodex.android.model.LocalizedAssignment;
import io.github.recodex.android.model.SolutionEvaluation;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.users.UsersManager;
import io.github.recodex.android.utils.LocalizationHelper;

public class AssignmentSolutionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ASSIGNMENT_ID = "assignmentId";

    @Inject
    UsersManager usersManager;
    @Inject
    ApiDataFetcher apiDataFetcher;
    @Inject
    LocalizationHelper localizationHelper;

    private String assignmentId;
    private OnSubmissionSelectedListener callback;
    private SwipeRefreshLayout swipeLayout = null;

    class SubmissionsListAdapter extends ArrayAdapter<AssignmentSolution> {

        private final List<AssignmentSolution> assignmentSolutions;
        private final LayoutInflater inflater;

        SubmissionsListAdapter(Context context, List<AssignmentSolution> assignmentSolutions) {
            super(context, R.layout.fragment_assignment_solutions);
            this.assignmentSolutions = assignmentSolutions;
            this.inflater = LayoutInflater.from(context);
            addAll(assignmentSolutions);
        }

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.assignment_submission_list_item, parent, false);
            } else {
                view = convertView;
            }

            // prepare vars
            LinearLayout submissionListItem = (LinearLayout) view.findViewById(R.id.assignment_submission_list_item);
            final AssignmentSolution assignmentSolution = assignmentSolutions.get(position);
            final AssignmentSolutionSubmission submission = assignmentSolution.getLastSubmission();

            // fill date
            String submittedAt = localizationHelper.getDateTime(assignmentSolution.getSolution().getCreatedAt());
            TextView submissionDate = (TextView) view.findViewById(R.id.assignment_submission_date);
            submissionDate.setText(submittedAt);

            if (submission.getEvaluation() != null) {
                SolutionEvaluation evaluation = submission.getEvaluation();

                // prepare and fill percentual score of submission
                int percentualScore = (int) (evaluation.getScore() * 100);
                TextView submissionValidity = (TextView) view.findViewById(R.id.assignment_submission_validity);
                submissionValidity.setText(String.format(Locale.ROOT, "%d%%", percentualScore));

                // get actual points and maybe bonus one and display them
                String points = evaluation.getPoints() + "/" + assignmentSolution.getMaxPoints();
                if (assignmentSolution.getBonusPoints() > 0) {
                    points = evaluation.getPoints() + "+" + assignmentSolution.getBonusPoints() + "/" + assignmentSolution.getMaxPoints();
                }
                TextView submissionPoints = (TextView) view.findViewById(R.id.assignment_submission_points);
                submissionPoints.setText(points);

                // set state symbol depending on the evaluation and solution
                ImageView stateImage = (ImageView) view.findViewById(R.id.assignment_submission_state_icon);
                if (evaluation.getScore() > 0) {
                    stateImage.setImageResource(R.drawable.ic_check_black_24dp);
                    stateImage.setColorFilter(getResources().getColor(R.color.colorGreen, null));
                } else {
                    stateImage.setImageResource(R.drawable.ic_clear_black_24dp);
                    stateImage.setColorFilter(getResources().getColor(R.color.colorRed, null));
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onSubmissionSelected(assignmentSolution.getId());
                    }
                }
            });
            return view;
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param assignmentId Parameter 1.
     * @return A new instance of fragment AssignmentTextFragment.
     */
    public static AssignmentSolutionsFragment newInstance(String assignmentId) {
        AssignmentSolutionsFragment fragment = new AssignmentSolutionsFragment();
        Bundle args = new Bundle();
        args.putString(ASSIGNMENT_ID, assignmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);

        if (getArguments() != null) {
            assignmentId = getArguments().getString(ASSIGNMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_assignment_solutions, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        swipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark);
        swipeLayout.setNestedScrollingEnabled(true);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    private void renderData(SubmissionsAssignmentPair pair) {
        LocalizedAssignment localizedAssignment = localizationHelper.getUserLocalizedText(pair.assignment.getLocalizedTexts());
        String assignmentName = localizedAssignment != null ? localizedAssignment.getName() : "";

        getActivity().setTitle("Solutions: " + assignmentName);
        ((ListView) getView().findViewById(R.id.assignment_solutions)).setAdapter(
                new SubmissionsListAdapter(getContext(), pair.assignmentSolutions));
    }

    private static class SubmissionsAssignmentPair {
        public final Assignment assignment;
        public final List<AssignmentSolution> assignmentSolutions;

        public SubmissionsAssignmentPair(Assignment assignment, List<AssignmentSolution> assignmentSolutions) {
            this.assignment = assignment;
            this.assignmentSolutions = assignmentSolutions;
        }
    }

    @Override
    public void onRefresh() {
        if (usersManager.getCurrentUser() != null) {
            new AsyncTask<Void, Void, SubmissionsAssignmentPair>() {
                @Override
                protected SubmissionsAssignmentPair doInBackground(Void... params) {
                    List<AssignmentSolution> assignmentSolutions = apiDataFetcher.fetchRemoteAssignmentSubmissions(usersManager.getCurrentUser(), assignmentId);
                    Assignment assignment = apiDataFetcher.fetchRemoteAssignment(assignmentId);
                    return new SubmissionsAssignmentPair(assignment, assignmentSolutions);
                }

                @Override
                protected void onPostExecute(SubmissionsAssignmentPair pair) {
                    if (pair.assignmentSolutions == null || pair.assignment == null) {
                        Toast.makeText(getContext(), R.string.assignment_submissions_loading_failed, Toast.LENGTH_SHORT).show();
                        swipeLayout.setRefreshing(false);
                        return;
                    }

                    renderData(pair);
                    swipeLayout.setRefreshing(false);
                }
            }.execute();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new AsyncTask<Void, Void, SubmissionsAssignmentPair>() {
            @Override
            protected SubmissionsAssignmentPair doInBackground(Void... params) {
                List<AssignmentSolution> assignmentSolutions = apiDataFetcher.fetchCachedAssignmentSubmissions(usersManager.getCurrentUser(), assignmentId);
                Assignment assignment = apiDataFetcher.fetchCachedAssignment(assignmentId);
                return new SubmissionsAssignmentPair(assignment, assignmentSolutions);
            }

            @Override
            protected void onPostExecute(SubmissionsAssignmentPair pair) {
                if (pair.assignmentSolutions != null && pair.assignment != null) {
                    renderData(pair);
                } else {
                    startForcedReload();
                }
            }
        }.execute();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmissionSelectedListener) {
            callback = (OnSubmissionSelectedListener) context;
        }
    }

    private void startForcedReload() {
        swipeLayout.setRefreshing(true);
        onRefresh();
    }

    public interface OnSubmissionSelectedListener {
        void onSubmissionSelected(String submissionId);
    }
}
