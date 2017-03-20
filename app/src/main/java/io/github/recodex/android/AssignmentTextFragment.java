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

import com.mukesh.MarkdownView;

import java.io.IOException;

import javax.inject.Inject;

import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssignmentTextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignmentTextFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String ASSIGNMENT_ID = "assignmentId";

    private String assignmentId;

    private final AssignmentTextFragment fragment = this;

    @Inject
    ApiWrapper<RecodexApi> api;

    private SwipeRefreshLayout swipeLayout = null;

    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, Assignment>() {
            protected Assignment doInBackground(Void... params) {
                return fetchAssignmentData(api.fromRemote());
            }

            protected void onPostExecute(Assignment assignment) {
                if (assignment == null) {
                    Toast.makeText(fragment.getContext(), R.string.loading_assignment_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                renderData(assignment);
                swipeLayout.setRefreshing(false);
            }
        }.execute();
    }

    public AssignmentTextFragment() {
        // Required empty public constructor
    }

    private Assignment fetchAssignmentData(RecodexApi api) {
        try {
            Response<Envelope<Assignment>> response = api.getAssignment(assignmentId).execute();

            if (!response.isSuccessful() || !response.body().isSuccess()) {
                return null;
            }

            return response.body().getPayload();
        } catch (IOException e) {
            return null;
        }
    }

    private void renderData(Assignment assignment) {
        getActivity().setTitle(assignment.getName());

        // TODO pick the correct locale
        String text = assignment.getLocalizedTexts().get(0).getText();

        try {
            ((MarkdownView) fragment.getView().findViewById(R.id.assignment_text))
                    .setMarkDownText(text);
        } catch (NullPointerException e) {}
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param assignmentId Parameter 1.
     * @return A new instance of fragment AssignmentTextFragment.
     */
    public static AssignmentTextFragment newInstance(String assignmentId) {
        AssignmentTextFragment fragment = new AssignmentTextFragment();
        Bundle args = new Bundle();
        args.putString(ASSIGNMENT_ID, assignmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            assignmentId = getArguments().getString(ASSIGNMENT_ID);
        }

        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_assignment_text, container, false);
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
        new AsyncTask<Void, Void, Assignment>() {
            @Override
            protected Assignment doInBackground(Void... params) {
                return fetchAssignmentData(api.fromCache());
            }

            @Override
            protected void onPostExecute(Assignment assignment) {
                if (assignment != null) {
                    renderData(assignment);
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
