package io.github.recodex.android.model;


public class UserPrivateData {
    private String email;
    private UserGroupIds groups;
    private UserSettings settings;

    public String getEmail() {
        return email;
    }

    public UserGroupIds getGroups() {
        return groups;
    }

    public UserSettings getSettings() {
        return settings;
    }
}
