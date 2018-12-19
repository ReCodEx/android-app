package io.github.recodex.android.model;

public class AssignmentSolutionSubmission {

    private long submittedAt;
    private String submittedBy;
    private SolutionEvaluation evaluation;
    private String evaluationStatus;

    public long getSubmittedAt() {
        return submittedAt;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public SolutionEvaluation getEvaluation() {
        return evaluation;
    }

    public String getEvaluationStatus() {
        return evaluationStatus;
    }
}
