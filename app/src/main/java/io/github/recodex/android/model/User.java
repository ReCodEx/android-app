package io.github.recodex.android.model;


public class User {
    private String fullName;
    private String id;
    private String avatarUrl;
    private UserPrivateData privateData;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserPrivateData getPrivateData() {
        return privateData;
    }
}
