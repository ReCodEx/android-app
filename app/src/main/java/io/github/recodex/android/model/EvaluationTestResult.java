package io.github.recodex.android.model;

public class EvaluationTestResult {
    private String id;
    private String testName;
    private String status;
    private double score;
    private boolean memoryExceeded;
    private boolean wallTimeExceeded;
    private boolean cpuTimeExceeded;
    private String message;

    public String getId() {
        return id;
    }

    public String getTestName() {
        return testName;
    }

    public String getStatus() {
        return status;
    }

    public double getScore() {
        return score;
    }

    public boolean getMemoryExceeded() {
        return memoryExceeded;
    }

    public boolean getTimeExceeded() {
        return wallTimeExceeded || cpuTimeExceeded;
    }

    public String getMessage() {
        return message;
    }
}
