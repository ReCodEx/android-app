package io.github.recodex.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Submission;
import io.github.recodex.android.model.User;
import io.github.recodex.android.users.ApiDataFetcher;
import retrofit2.Response;
import us.feras.mdv.MarkdownView;


public class SubmissionFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String SUBMISSION_ID = "submissionId";

    private String submissionId;

    @Inject
    ApiDataFetcher apiDataFetcher;

    private SwipeRefreshLayout swipeLayout = null;

    private void renderData(AsyncResultStruct asyncResultStruct) {
        getActivity().setTitle("Evaluation: " + asyncResultStruct.assignment.getName());

        // TODO
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
        public Submission submission;
        public Assignment assignment;

        public AsyncResultStruct(User submittedBy, Submission submission, Assignment assignment) {
            this.submittedBy = submittedBy;
            this.submission = submission;
            this.assignment = assignment;
        }
    }

    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, AsyncResultStruct>() {
            protected AsyncResultStruct doInBackground(Void... params) {
                Submission submission = apiDataFetcher.fetchRemoteSubmission(submissionId);
                Assignment assignment = null;
                User user = null;
                if (submission != null) {
                    assignment = apiDataFetcher.fetchRemoteAssignment(submission.getExerciseAssignmentId());
                    user = apiDataFetcher.fetchRemoteUser(submission.getUserId());
                }
                return new AsyncResultStruct(user, submission, assignment);
            }

            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment == null || asyncResultStruct.submission == null ||
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
                Submission submission = apiDataFetcher.fetchCachedSubmission(submissionId);
                Assignment assignment = null;
                User user = null;
                if (submission != null) {
                    assignment = apiDataFetcher.fetchCachedAssignment(submission.getExerciseAssignmentId());
                    user = apiDataFetcher.fetchCachedUser(submission.getUserId());
                }
                return new AsyncResultStruct(user, submission, assignment);
            }

            @Override
            protected void onPostExecute(AsyncResultStruct asyncResultStruct) {
                if (asyncResultStruct.assignment != null && asyncResultStruct.submittedBy != null &&
                        asyncResultStruct.submission != null) {
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
}
