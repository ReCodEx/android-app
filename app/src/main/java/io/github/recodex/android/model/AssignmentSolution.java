package io.github.recodex.android.model;

public class AssignmentSolution {
    private String id;
    private String note;
    private AssignmentSolutionSubmission lastSubmission;
    private int maxPoints;
    private String exerciseAssignmentId;

    public String getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public AssignmentSolutionSubmission getLastSubmission() {
        return lastSubmission;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public String getExerciseAssignmentId() {
        return exerciseAssignmentId;
    }
}
