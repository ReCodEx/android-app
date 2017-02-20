package io.github.recodex.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import javax.inject.Inject;

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import retrofit2.Response;


/**
 * Displays details of a group
 */
public class GroupDetailFragment extends Fragment {
    private static final String ARG_GROUP_ID = "groupId";

    @Inject
    RecodexApi api;

    private String groupId;

    private GroupDetailFragment fragment = this;

    class LoadGroupTask extends AsyncTask<Void, Void, Group> {
        protected Group doInBackground(Void... params) {
            try {
                Response<Envelope<Group>> response = api.getGroup(groupId).execute();

                if (!response.isSuccessful()) {
                    return null;
                }

                return response.body().getPayload();
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Group group) {
            if (group == null) {
                // TODO handle this
                return;
            }

            ((TextView) fragment.getView().findViewById(R.id.group_name)).setText(group.getName());
            getActivity().setTitle(group.getName());
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

        new LoadGroupTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_detail, container, false);
    }
}
