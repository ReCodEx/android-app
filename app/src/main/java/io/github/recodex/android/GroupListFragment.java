package io.github.recodex.android;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.UserGroups;
import io.github.recodex.android.users.UsersManager;
import retrofit2.Response;

/**
 * Displays groups the user belongs to, along with some useful information
 */
public class GroupListFragment extends ListFragment {
    @Inject
    RecodexApi api;

    @Inject
    UsersManager users;

    ListFragment fragment = this;

    class LoadGroupsTask extends AsyncTask<Void, Void, List<Group>> {
        protected List<Group> doInBackground(Void... params) {
            try {
                Response<Envelope<UserGroups>> response = api.getGroupsForUser(users.getCurrentUser().getId()).execute();

                if (!response.isSuccessful()) {
                    return null;
                }

                Envelope<UserGroups> body = response.body();
                return body.getPayload().getStudent();
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(List<Group> groups) {
            if (groups == null) {
                // TODO loading failed - do something smart
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_list_item_1);
            for (Group group : groups) {
                adapter.add(group.getName());
            }

            fragment.setListAdapter(adapter);
            fragment.setListShown(true);
        }
    }

    public GroupListFragment() {
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);

        setListAdapter(null);

        new LoadGroupsTask().execute();

        // ListView view = getListView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);
        parent.addView(v, 0);
        return parent;
    }
}
