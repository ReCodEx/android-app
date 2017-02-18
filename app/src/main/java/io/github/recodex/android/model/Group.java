package io.github.recodex.android.model;

public class Group {
    private String name;
    private String description;
    private GroupAssignments assignments;

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
