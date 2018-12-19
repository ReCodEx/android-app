package io.github.recodex.android.model;

public class AssignmentSolution {
    private String id;
    private String note;
    private long submittedAt;
    private SolutionEvaluation evaluation;
    private String evaluationStatus;
    private int maxPoints;
    private String exerciseAssignmentId;
    private String userId;

    public String getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public SolutionEvaluation getEvaluation() {
        return evaluation;
    }

    public String getEvaluationStatus() {
        return evaluationStatus;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public String getExerciseAssignmentId() {
        return exerciseAssignmentId;
    }

    public String getUserId() {
        return userId;
    }
}
