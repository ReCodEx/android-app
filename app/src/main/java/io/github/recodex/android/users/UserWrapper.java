package io.github.recodex.android.users;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.github.recodex.android.R;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.StudentGroupStats;


public class UserWrapper {

    private static final String FULL_NAME = "FULL_NAME";
    private static final String AVATAR_URL = "AVATAR_URL";
    private static final String GROUP_LIST = "GROUP_LIST";
    private static final String GROUPS_STATS = "GROUPS_STATS";
    private static final String DEFAULT_LANGUAGE_KEY = "DEFAULT_LANGUAGE";
    private static final String DEFAULT_LANGUAGE = "en";

    private final Context context;
    private final SharedPreferences preferences;
    private final Account account;
    private final String id;
    private final LoginType loginType;

    public UserWrapper(Context context, String id, Account account, String loginType) {
        this.context = context;
        this.id = id;
        this.loginType = LoginType.stringToType(loginType);
        this.account = account;
        this.preferences = context.getSharedPreferences(context.getString(R.string.user_preferences_prefix) + id, Context.MODE_PRIVATE);
    }

    public Account getAccount() {
        return account;
    }

    public void requestSync() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // perform even if off
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // perform even if off
        ContentResolver.requestSync(account, ReCodExAuthenticator.PROVIDER_AUTHORITY, bundle);
    }

    public void setSyncInterval(long minutes) {
        ContentResolver.addPeriodicSync(account, ReCodExAuthenticator.PROVIDER_AUTHORITY, Bundle.EMPTY, minutes * 60);
        Log.d(context.getString(R.string.recodex_log_tag), "Sync interval changed: " + minutes + " min");
    }

    public String getId() {
        return id;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public String getFullName() {
        return preferences.getString(FULL_NAME, context.getString(R.string.john_doe));
    }

    public String getAvatarUrl() {
        return preferences.getString(AVATAR_URL, "");
    }

    public String getDefaultLanguage() {
        return preferences.getString(DEFAULT_LANGUAGE_KEY, DEFAULT_LANGUAGE);
    }

    public void updateData(Login login) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FULL_NAME, login.getUser().getFullName());
        editor.putString(AVATAR_URL, login.getUser().getAvatarUrl());
        editor.putString(DEFAULT_LANGUAGE_KEY, login.getUser().getPrivateData().getSettings().getDefaultLanguage());
        editor.apply();
    }

    public void setGroupsInfo(List<Group> groups, List<StudentGroupStats> stats) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GROUP_LIST, new Gson().toJson(groups));
        editor.putString(GROUPS_STATS, new Gson().toJson(stats));
        editor.apply();
    }

    public List<StudentGroupStats> getGroupStats() {
        if (!preferences.contains(GROUPS_STATS)) {
            return null;
        }
        String groupsJson = preferences.getString(GROUPS_STATS, "");
        Type type = new TypeToken<List<StudentGroupStats>>() {
        }.getType();
        return new Gson().fromJson(groupsJson, type);
    }

    public List<Group> getGroups() {
        if (!preferences.contains(GROUP_LIST)) {
            return null;
        }
        String groupsJson = preferences.getString(GROUP_LIST, "");
        Type type = new TypeToken<List<Group>>() {
        }.getType();
        return new Gson().fromJson(groupsJson, type);
    }

    public Group getGroup(String groupId) {
        if (!preferences.contains(GROUP_LIST)) {
            return null;
        }
        String groupsJson = preferences.getString(GROUP_LIST, "");
        Type type = new TypeToken<List<Group>>() {
        }.getType();
        List<Group> groups = new Gson().fromJson(groupsJson, type);

        for (Group group : groups) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }

        return null;
    }
}
