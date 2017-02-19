package io.github.recodex.android.model;

public class Group {
    private String id;
    private String name;
    private String description;
    private GroupAssignments assignments;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GroupAssignments getAssignments() {
        return assignments;
    }
}
