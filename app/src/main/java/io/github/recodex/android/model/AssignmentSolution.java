package io.github.recodex.android.model;

public class AssignmentSolution {
    private String id;
    private String note;
    private Solution solution;
    private AssignmentSolutionSubmission lastSubmission;
    private int maxPoints;
    private String exerciseAssignmentId;

    public String getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public Solution getSolution() {
        return solution;
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
