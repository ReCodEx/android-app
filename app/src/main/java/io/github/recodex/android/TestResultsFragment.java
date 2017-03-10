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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.SolutionEvaluation;
import io.github.recodex.android.model.Submission;
import io.github.recodex.android.model.User;
import io.github.recodex.android.users.ApiDataFetcher;


public class TestResultsFragment extends Fragment {
    private static final String SUBMISSION_ID = "submissionId";

    private String submissionId;

    @Inject
    ApiDataFetcher apiDataFetcher;

    private void renderData(AsyncResultStruct asyncResultStruct) {
        Submission submission = asyncResultStruct.submission;
        Assignment assignment = asyncResultStruct.assignment;

        getActivity().setTitle("Evaluation: " + assignment.getName());

        // TODO
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
        public Submission submission;
        public Assignment assignment;

        public AsyncResultStruct(Submission submission, Assignment assignment) {
            this.submission = submission;
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
                Submission submission = apiDataFetcher.fetchCachedSubmission(submissionId);
                Assignment assignment = null;
                if (submission != null) {
                    assignment = apiDataFetcher.fetchCachedAssignment(submission.getExerciseAssignmentId());
                }
                return new AsyncResultStruct(submission, assignment);
            }

            @Override
            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment != null && asyncResultStruct.submission != null) {
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
                Submission submission = apiDataFetcher.fetchRemoteSubmission(submissionId);
                Assignment assignment = null;
                if (submission != null) {
                    assignment = apiDataFetcher.fetchRemoteAssignment(submission.getExerciseAssignmentId());
                }
                return new AsyncResultStruct(submission, assignment);
            }

            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment == null || asyncResultStruct.submission == null) {
                    Toast.makeText(getContext(), R.string.submission_loading_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                renderData(asyncResultStruct);
            }
        }.execute();
    }
}
