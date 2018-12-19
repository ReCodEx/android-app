package io.github.recodex.android.model;

public class Group {
    private String id;
    private String name;
    private GroupPrivateData privateData;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return privateData.getDescription();
    }

    public GroupPrivateData getPrivateData() {
        return privateData;
    }
}
