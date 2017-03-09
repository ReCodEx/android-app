package io.github.recodex.android.users;

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.List;

import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Submission;
import io.github.recodex.android.model.UserGroups;
import retrofit2.Response;

public class ApiDataFetcher {

    private ApiWrapper<RecodexApi> apiWrapper;
    private RecodexApi recodexApi;

    public ApiDataFetcher(RecodexApi recodexApi, ApiWrapper<RecodexApi> apiWrapper) {
        this.recodexApi = recodexApi;
        this.apiWrapper = apiWrapper;
    }

    public UserGroups fetchAndStoreGroups(UserWrapper user) {
        Response<Envelope<UserGroups>> response = null;
        try {
            response = recodexApi.getGroupsForUser(user.getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isSuccessful()) {
            UserGroups userGroups = response.body().getPayload();
            user.setGroupsInfo(userGroups.getStudent(), userGroups.getStats());
            return userGroups;
        }

        return null;
    }

    private List<Submission> getAssignmentSubmissions(RecodexApi api, UserWrapper user, String assignmentId) {
        try {
            Response<Envelope<List<Submission>>> response = api.getAssignmentSubmissions(assignmentId, user.getId()).execute();

            if (!response.isSuccessful() || !response.body().isSuccess()) {
                return null;
            }

            return response.body().getPayload();
        } catch (IOException e) {
            return null;
        }
    }

    public List<Submission> fetchRemoteAssignmentSubmissions(UserWrapper user, String assignmentId) {
        return getAssignmentSubmissions(apiWrapper.fromRemote(), user, assignmentId);
    }

    public List<Submission> fetchCachedAssignmentSubmissions(UserWrapper user, String assignmentId) {
        return getAssignmentSubmissions(apiWrapper.fromCache(), user, assignmentId);
    }

    private Assignment getAssignment(RecodexApi api, String assignmentId) {
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

    public Assignment fetchRemoteAssignment(String assignmentId) {
        return getAssignment(apiWrapper.fromRemote(), assignmentId);
    }

    public Assignment fetchCachedAssignment(String assignmentId) {
        return getAssignment(apiWrapper.fromCache(), assignmentId);
    }

    public void fetchAndStoreAll(UserWrapper user) {
        fetchAndStoreGroups(user);
    }
}
