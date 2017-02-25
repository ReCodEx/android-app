package io.github.recodex.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import io.github.recodex.android.model.Group;
import io.github.recodex.android.users.UsersManager;


/**
 * Displays details of a group
 */
public class GroupDetailFragment extends Fragment {
    private static final String ARG_GROUP_ID = "groupId";

    @Inject
    UsersManager usersManager;

    private String groupId;

    private GroupDetailFragment fragment = this;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);
        fillGroupInfo(view);
        return view;
    }

    private void fillGroupInfo(View view) {
        if (usersManager.getCurrentUser() == null) {
            return;
        }

        // get group saved under current user
        Group group = usersManager.getCurrentUser().getGroup(groupId);

        // group cannot be found
        if (group == null) {
            // TODO handle this
            return;
        }

        ((TextView) view.findViewById(R.id.group_name)).setText(group.getName());
        getActivity().setTitle(group.getName());
    }
}
