package io.github.recodex.android.model;

import java.util.List;

public class UserGroups {
    private List<Group> student;
    private List<Group> supervisor;
    private List<StudentGroupStats> stats;

    public List<Group> getStudent() {
        return student;
    }

    public List<Group> getSupervisor() {
        return supervisor;
    }

    public List<StudentGroupStats> getStats() {
        return stats;
    }
}
