package io.github.recodex.android.users;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.recodex.android.NavigationDrawer;
import io.github.recodex.android.R;
import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Assignment;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.Submission;
import io.github.recodex.android.model.UserGroups;
import retrofit2.Response;

public class ApiDataFetcher {

    private ApiWrapper<RecodexApi> apiWrapper;
    private RecodexApi recodexApi;
    private Context applicationContext;
    private final String NOTIFICATION_DATA = "recodex_notification_data";

    public ApiDataFetcher(RecodexApi recodexApi, ApiWrapper<RecodexApi> apiWrapper, Context applicationContext) {
        this.recodexApi = recodexApi;
        this.apiWrapper = apiWrapper;
        this.applicationContext = applicationContext;
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
        UserGroups groups = fetchAndStoreGroups(user);
        for (Group g : groups.getStudent()) {
            List<String> assignmentList = g.getAssignments().getPublic();
            List<String> oldAssignmentList = getAssignmentList(g.getId());
            for (String assignmentId : assignmentList) {
                Assignment a = getAssignment(apiWrapper.fromRemote(), assignmentId);
                if (!oldAssignmentList.contains(assignmentId)) {
                    // make a notification
                    makeNotification(a, g.getName());
                }
            }
            setAssignmentList(assignmentList, g.getId());
        }
    }

    private void makeNotification(Assignment a, String groupName) {
        Intent resultIntent = new Intent(applicationContext, NavigationDrawer.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                applicationContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_logo_vector)
                .setContentTitle(applicationContext.getString(R.string.new_assignment) + " '" + groupName + "'")
                .setContentText(a.getName())
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(a.getId().hashCode(), notificationBuilder.build());
    }

    private void setAssignmentList(List<String> assignmentIds, String groupId) {
        SharedPreferences preferences = applicationContext.getSharedPreferences(NOTIFICATION_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(groupId, new Gson().toJson(assignmentIds));
        editor.commit();
    }

    private List<String> getAssignmentList(String groupId) {
        SharedPreferences preferences = applicationContext.getSharedPreferences(NOTIFICATION_DATA, Context.MODE_PRIVATE);
        if (!preferences.contains(groupId)) {
            return new ArrayList<>();
        }
        String groupsJson = preferences.getString(groupId, "");
        Type type = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(groupsJson, type);
    }
}
