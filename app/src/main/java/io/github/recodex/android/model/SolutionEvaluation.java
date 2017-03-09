package io.github.recodex.android.model;

public class SolutionEvaluation {
    private String id;
    private long evaluatedAt;
    private float score;
    private int points;

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
}
