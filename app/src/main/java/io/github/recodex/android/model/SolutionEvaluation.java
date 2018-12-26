package io.github.recodex.android.model;

import java.util.List;

public class SolutionEvaluation {
    private String id;
    private long evaluatedAt;
    private float score;
    private int points;
    private boolean initFailed;
    private boolean isValid;
    private boolean evaluationFailed;
    private List<EvaluationTestResult> testResults;

    public String getId() {
        return id;
    }

    public float getScore() {
        return score;
    }

    public int getPoints() {
        return points;
    }

    public long getEvaluatedAt() {
        return evaluatedAt;
    }

    public boolean getInitFailed() {
        return initFailed;
    }

    public List<EvaluationTestResult> getTestResults() {
        return testResults;
    }
}
