package io.github.recodex.android.model;


public class GroupPrivateData {
    private String description;
    private GroupAssignments assignments;

    public GroupAssignments getAssignments() {
        return assignments;
    }

    public void setAssignments(GroupAssignments assignments) {
        this.assignments = assignments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
