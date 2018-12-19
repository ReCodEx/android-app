package io.github.recodex.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.AssignmentSolution;
import io.github.recodex.android.model.EvaluationTestResult;
import io.github.recodex.android.model.LocalizedAssignment;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.utils.LocalizationHelper;


public class TestResultsFragment extends Fragment {
    private static final String SUBMISSION_ID = "submissionId";

    private String submissionId;

    @Inject
    ApiDataFetcher apiDataFetcher;

    @Inject
    LocalizationHelper localizationHelper;

    private void renderData(AsyncResultStruct asyncResultStruct) {
        AssignmentSolution assignmentSolution = asyncResultStruct.assignmentSolution;
        Assignment assignment = asyncResultStruct.assignment;
        List<EvaluationTestResult> testResults = assignmentSolution.getLastSubmission().getEvaluation().getTestResults();

        LocalizedAssignment localizedAssignment = localizationHelper.getUserLocalizedText(assignment.getLocalizedTexts());
        getActivity().setTitle("Results: " + (localizedAssignment != null ? localizedAssignment.getName() : ""));

        TableLayout table = (TableLayout) getView().findViewById(R.id.test_results_table);
        for (EvaluationTestResult testResult : testResults) {
            int padding = getResources().getDimensionPixelSize(R.dimen.test_results_row_padding);
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            row.setPadding(padding, padding, padding, padding);
            row.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            table.addView(row);

            TableRow.LayoutParams firstParams = new TableRow.LayoutParams(0);
            firstParams.gravity = Gravity.CENTER;
            TextView testName = new TextView(getActivity());
            testName.setText(testResult.getTestName());
            testName.setLayoutParams(firstParams);
            row.addView(testName);

            TableRow.LayoutParams secondParams = new TableRow.LayoutParams(1);
            secondParams.gravity = Gravity.CENTER;
            int percentualScore = (int) testResult.getScore() * 100;
            TextView score = new TextView(getActivity());
            score.setText(String.format(Locale.ROOT, "%d%%", percentualScore));
            score.setLayoutParams(secondParams);
            row.addView(score);

            TableRow.LayoutParams thirdParams = new TableRow.LayoutParams(2);
            thirdParams.gravity = Gravity.CENTER;
            ImageView memory = new ImageView(getActivity());
            if (testResult.getMemoryExceeded()) {
                memory.setImageResource(R.drawable.ic_clear_black_24dp);
                memory.setColorFilter(getResources().getColor(R.color.colorRed));
            } else {
                memory.setImageResource(R.drawable.ic_check_black_24dp);
                memory.setColorFilter(getResources().getColor(R.color.colorGreen));
            }
            memory.setLayoutParams(thirdParams);
            row.addView(memory);

            TableRow.LayoutParams fourthParams = new TableRow.LayoutParams(3);
            fourthParams.gravity = Gravity.CENTER;
            ImageView time = new ImageView(getActivity());
            if (testResult.getTimeExceeded()) {
                time.setImageResource(R.drawable.ic_clear_black_24dp);
                time.setColorFilter(getResources().getColor(R.color.colorRed));
            } else {
                time.setImageResource(R.drawable.ic_check_black_24dp);
                time.setColorFilter(getResources().getColor(R.color.colorGreen));
            }
            time.setLayoutParams(fourthParams);
            row.addView(time);
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param submissionId Parameter 1.
     * @return A new instance of fragment AssignmentTextFragment.
     */
    public static TestResultsFragment newInstance(String submissionId) {
        TestResultsFragment fragment = new TestResultsFragment();
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
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_test_results, container, false);
        return view;
    }

    class AsyncResultStruct {
        public AssignmentSolution assignmentSolution;
        public Assignment assignment;

        public AsyncResultStruct(AssignmentSolution assignmentSolution, Assignment assignment) {
            this.assignmentSolution = assignmentSolution;
            this.assignment = assignment;
        }
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
                if (assignmentSolution != null) {
                    assignment = apiDataFetcher.fetchCachedAssignment(assignmentSolution.getExerciseAssignmentId());
                }
                return new AsyncResultStruct(assignmentSolution, assignment);
            }

            @Override
            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment != null && asyncResultStruct.assignmentSolution != null) {
                    renderData(asyncResultStruct);
                } else {
                    startForcedReload();
                }
            }
        }.execute();
    }

    private void startForcedReload() {
        new AsyncTask<Void, Void, AsyncResultStruct>() {
            protected AsyncResultStruct doInBackground(Void... params) {
                AssignmentSolution assignmentSolution = apiDataFetcher.fetchRemoteAssignmentSolution(submissionId);
                Assignment assignment = null;
                if (assignmentSolution != null) {
                    assignment = apiDataFetcher.fetchRemoteAssignment(assignmentSolution.getExerciseAssignmentId());
                }
                return new AsyncResultStruct(assignmentSolution, assignment);
            }

            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment == null || asyncResultStruct.assignmentSolution == null) {
                    Toast.makeText(getContext(), R.string.submission_loading_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                renderData(asyncResultStruct);
            }
        }.execute();
    }
}
