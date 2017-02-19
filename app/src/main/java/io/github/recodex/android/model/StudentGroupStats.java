package io.github.recodex.android.model;

public class StudentGroupStats {

    private String groupId;
    private AssignmentStats assignments;
    private PointStats points;
    private boolean hasLimit;
    private boolean passesLimit;

    public String getGroupId() {
        return groupId;
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
