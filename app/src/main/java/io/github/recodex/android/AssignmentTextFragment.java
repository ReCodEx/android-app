package io.github.recodex.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import retrofit2.Response;
import us.feras.mdv.MarkdownView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssignmentTextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignmentTextFragment extends Fragment {
    private static final String ASSIGNMENT_ID = "assignmentId";

    private String assignmentId;

    private final AssignmentTextFragment fragment = this;

    @Inject
    RecodexApi api;

    class LoadAssignmentTask extends AsyncTask<Void, Void, Assignment> {
        protected Assignment doInBackground(Void... params) {
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

        protected void onPostExecute(Assignment assignment) {
            if (assignment == null) {
                Toast.makeText(fragment.getContext(), R.string.loading_assignment_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO pick the correct locale
            String text = assignment.getLocalizedTexts().get(0).getText();
            ((MarkdownView) fragment.getView().findViewById(R.id.assignment_text))
                    .loadMarkdown(text);
        }
    }

    public AssignmentTextFragment() {
        // Required empty public constructor
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

        new LoadAssignmentTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assignment_text, container, false);
    }
}
