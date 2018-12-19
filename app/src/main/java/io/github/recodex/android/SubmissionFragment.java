package io.github.recodex.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.AssignmentSolution;
import io.github.recodex.android.model.AssignmentSolutionSubmission;
import io.github.recodex.android.model.LocalizedAssignment;
import io.github.recodex.android.model.SolutionEvaluation;
import io.github.recodex.android.model.User;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.utils.LocalizationHelper;


public class SubmissionFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String SUBMISSION_ID = "submissionId";

    private String submissionId;

    @Inject
    ApiDataFetcher apiDataFetcher;

    @Inject
    LocalizationHelper localizationHelper;

    private SwipeRefreshLayout swipeLayout = null;
    private OnTestResultsSelectedListener testResultsCallback = null;

    private void renderData(AsyncResultStruct asyncResultStruct) {
        final AssignmentSolution assignmentSolution = asyncResultStruct.assignmentSolution;
        final AssignmentSolutionSubmission submission = assignmentSolution.getLastSubmission();
        Assignment assignment = asyncResultStruct.assignment;
        User user = asyncResultStruct.submittedBy;

        LocalizedAssignment localizedAssignment = localizationHelper.getUserLocalizedText(assignment.getLocalizedTexts());
        getActivity().setTitle("Evaluation: " + (localizedAssignment != null ? localizedAssignment.getName() : ""));

        String submittedAt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                .format(new Date(submission.getSubmittedAt() * 1000));
        TextView submittedAtView = (TextView) getView().findViewById(R.id.submission_submitted_at);
        submittedAtView.setText(submittedAt);

        TextView submissionAuthor = (TextView) getView().findViewById(R.id.submission_author);
        submissionAuthor.setText(user.getFullName());

        TextView submissionStatus = (TextView) getView().findViewById(R.id.submission_evaluation_status);
        submissionStatus.setText(submission.getEvaluationStatus());

        // display evaluation details if submission is evaluated
        if (submission.getEvaluation() != null) {
            SolutionEvaluation evaluation = submission.getEvaluation();

            // first hide and display appropriate layouts
            LinearLayout evaluationNotReady = (LinearLayout) getView().findViewById(R.id.submission_evaluation_not_complete_area);
            LinearLayout evaluationReady = (LinearLayout) getView().findViewById(R.id.submission_evaluation_area);
            evaluationNotReady.setVisibility(View.GONE);
            evaluationReady.setVisibility(View.VISIBLE);

            String evaluatedAt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                    .format(new Date(evaluation.getEvaluatedAt() * 1000));
            TextView evaluatedAtView = (TextView) getView().findViewById(R.id.submission_evaluated_at);
            evaluatedAtView.setText(evaluatedAt);

            int percentual_score = (int) evaluation.getScore() * 100;
            TextView score = (TextView) getView().findViewById(R.id.submission_score);
            score.setText(String.format(Locale.ROOT, "%d%%", percentual_score));

            TextView points = (TextView) getView().findViewById(R.id.submission_points);
            points.setText(String.format(Locale.ROOT, "%d/%d", evaluation.getPoints(), assignmentSolution.getMaxPoints()));

            TextView bonusPoints = (TextView) getView().findViewById(R.id.submission_bonus_points);
            bonusPoints.setText(String.format(Locale.ROOT, "+%d", evaluation.getBonusPoints()));

            if (!evaluation.getEvaluationFailed()) {
                ImageView evaluationFailed = (ImageView) getView().findViewById(R.id.submission_evaluation_finished);
                evaluationFailed.setImageResource(R.drawable.ic_check_black_24dp);
                evaluationFailed.setColorFilter(getResources().getColor(R.color.colorGreen));
            }

            if (!evaluation.getInitFailed()) {
                ImageView buildSucceeded = (ImageView) getView().findViewById(R.id.submission_build_succeeded);
                buildSucceeded.setImageResource(R.drawable.ic_check_black_24dp);
                buildSucceeded.setColorFilter(getResources().getColor(R.color.colorGreen));
            }

            if (evaluation.getIsValid()) {
                ImageView evaluationValid = (ImageView) getView().findViewById(R.id.submission_evaluation_valid);
                evaluationValid.setImageResource(R.drawable.ic_check_black_24dp);
                evaluationValid.setColorFilter(getResources().getColor(R.color.colorGreen));
            }

            getView().findViewById(R.id.display_test_results_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (testResultsCallback != null) {
                        testResultsCallback.onTestResultsSelected(assignmentSolution.getId());
                    }
                }
            });
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param submissionId Parameter 1.
     * @return A new instance of fragment AssignmentTextFragment.
     */
    public static SubmissionFragment newInstance(String submissionId) {
        SubmissionFragment fragment = new SubmissionFragment();
        Bundle args = new Bundle();
        args.putString(SUBMISSION_ID, submissionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            submissionId = getArguments().getString(SUBMISSION_ID);
        }

        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_submission, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        swipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryDark);
        swipeLayout.setNestedScrollingEnabled(true);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    class AsyncResultStruct {
        public User submittedBy;
        public AssignmentSolution assignmentSolution;
        public Assignment assignment;

        public AsyncResultStruct(User submittedBy, AssignmentSolution assignmentSolution, Assignment assignment) {
            this.submittedBy = submittedBy;
            this.assignmentSolution = assignmentSolution;
            this.assignment = assignment;
        }
    }

    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, AsyncResultStruct>() {
            protected AsyncResultStruct doInBackground(Void... params) {
                AssignmentSolution assignmentSolution = apiDataFetcher.fetchRemoteAssignmentSolution(submissionId);
                Assignment assignment = null;
                User user = null;
                if (assignmentSolution != null) {
                    assignment = apiDataFetcher.fetchRemoteAssignment(assignmentSolution.getExerciseAssignmentId());
                    user = apiDataFetcher.fetchRemoteUser(assignmentSolution.getLastSubmission().getSubmittedBy());
                }
                return new AsyncResultStruct(user, assignmentSolution, assignment);
            }

            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment == null || asyncResultStruct.assignmentSolution == null ||
                        asyncResultStruct.submittedBy == null) {
                    Toast.makeText(getContext(), R.string.submission_loading_failed, Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                    return;
                }

                renderData(asyncResultStruct);
                swipeLayout.setRefreshing(false);
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Try to load data from the cache so that there is something to display
        new AsyncTask<Void, Void, AsyncResultStruct>() {
            @Override
            protected AsyncResultStruct doInBackground(Void... params) {
                AssignmentSolution assignmentSolution = apiDataFetcher.fetchCachedAssignmentSolution(submissionId);
                Assignment assignment = null;
                User user = null;
                if (assignmentSolution != null) {
                    assignment = apiDataFetcher.fetchCachedAssignment(assignmentSolution.getExerciseAssignmentId());
                    user = apiDataFetcher.fetchCachedUser(assignmentSolution.getLastSubmission().getSubmittedBy());
                }
                return new AsyncResultStruct(user, assignmentSolution, assignment);
            }

            @Override
            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment != null && asyncResultStruct.submittedBy != null &&
                        asyncResultStruct.assignmentSolution != null) {
                    renderData(asyncResultStruct);
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
        if (context instanceof OnTestResultsSelectedListener) {
            testResultsCallback = (OnTestResultsSelectedListener) context;
        }
    }

    public interface OnTestResultsSelectedListener {
        void onTestResultsSelected(String submissiontId);
    }
}
