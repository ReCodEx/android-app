package io.github.recodex.android.model;

import java.util.List;

public class StudentGroupStats {

    private String groupId;
    private List<AssignmentStats> assignments;
    private PointStats points;
    private boolean hasLimit;
    private boolean passesLimit;

    public String getGroupId() {
        return groupId;
    }

    public List<AssignmentStats> getAssignments() {
        return assignments;
    }

    public int getTotalPoints() {
        return points.getTotal();
    }

    public int getGainedPoints() {
        return points.getGained();
    }

    public boolean getHasLimit() {
        return hasLimit;
    }

    public boolean getPassesLimit() {
        return passesLimit;
    }
}
