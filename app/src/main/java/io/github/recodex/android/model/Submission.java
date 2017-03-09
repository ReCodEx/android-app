package io.github.recodex.android.model;

public class Submission {
    private String id;
    private String note;
    private long submittedAt;
    private SolutionEvaluation evaluation;
    private int maxPoints;

    public String getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public SolutionEvaluation getEvaluation() {
        return evaluation;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public int getMaxPoints() {
        return maxPoints;
    }
}
