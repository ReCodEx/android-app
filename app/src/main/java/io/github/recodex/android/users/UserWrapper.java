package io.github.recodex.android.users;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.github.recodex.android.R;
import io.github.recodex.android.api.Constants;
import io.github.recodex.android.model.Group;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.model.StudentGroupStats;


public class UserWrapper {

    private static final String FULL_NAME = "FULL_NAME";
    private static final String AVATAR_URL = "AVATAR_URL";
    private static final String GROUP_LIST = "GROUP_LIST";
    private static final String GROUPS_STATS = "GROUPS_STATS";

    private Context context;
    private SharedPreferences preferences;
    private Account account;
    private String id;
    private LoginType loginType;

    public UserWrapper(Context context, String id, Account account, String loginType) {
        this.context = context;
        this.id = id;
        this.loginType = LoginType.stringToType(loginType);
        this.account = account;
        this.preferences = context.getSharedPreferences(context.getString(R.string.user_preferences_prefix) + id, Context.MODE_PRIVATE);
    }

    public Account getAccount() { return account; }

    public String getId() { return id; }

    public LoginType getLoginType() { return loginType; }

    public String getFullName() {
        return preferences.getString(FULL_NAME, context.getString(R.string.john_doe));
    }

    public String getAvatarUrl() {
        return preferences.getString(AVATAR_URL, "");
    }

    public void updateData(Login login) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FULL_NAME, login.getUser().getFullName());
        editor.putString(AVATAR_URL, login.getUser().getAvatarUrl());
        editor.commit();
    }

    public void setGroupsInfo(List<Group> groups, List<StudentGroupStats> stats) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GROUP_LIST, new Gson().toJson(groups));
        editor.putString(GROUPS_STATS, new Gson().toJson(stats));
        editor.commit();
    }

    public List<StudentGroupStats> getGroupStats() {
        if (!preferences.contains(GROUPS_STATS)) {
            return null;
        }
        String groupsJson = preferences.getString(GROUPS_STATS, "");
        Type type = new TypeToken<List<StudentGroupStats>>() {}.getType();
        return new Gson().fromJson(groupsJson, type);
    }

    public List<Group> getGroups() {
        if (!preferences.contains(GROUP_LIST)) {
            return null;
        }
        String groupsJson = preferences.getString(GROUP_LIST, "");
        Type type = new TypeToken<List<Group>>() {}.getType();
        return new Gson().fromJson(groupsJson, type);
    }
}
