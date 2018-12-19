package io.github.recodex.android.model;


import java.util.List;

public class GroupPrivateData {
    private String description;
    private List<String> assignments;

    public List<String> getAssignments() {
        return assignments;
    }

    public String getDescription() {
        return description;
    }
}
