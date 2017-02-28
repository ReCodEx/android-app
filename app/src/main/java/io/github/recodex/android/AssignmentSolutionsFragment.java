package io.github.recodex.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssignmentSolutionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignmentSolutionsFragment extends Fragment {
    private static final String ASSIGNMENT_ID = "assignmentId";

    private String assignmentId;

    public AssignmentSolutionsFragment() {
        // Required empty public constructor
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
        if (getArguments() != null) {
            assignmentId = getArguments().getString(ASSIGNMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assignment_solutions, container, false);
    }
}