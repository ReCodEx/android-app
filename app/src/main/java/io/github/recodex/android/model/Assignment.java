package io.github.recodex.android.model;

import java.util.List;

public class Assignment {
    private String id;
    private List<LocalizedAssignment> localizedTexts;
    private long firstDeadline;
    private long secondDeadline;
    private boolean allowSecondDeadline;

    public String getId() {
        return id;
    }

    public List<LocalizedAssignment> getLocalizedTexts() {
        return localizedTexts;
    }

    public long getFirstDeadline() {
        return firstDeadline;
    }

    public long getSecondDeadline() {
        if (allowSecondDeadline) {
            return secondDeadline;
        } else {
            return 0;
        }
    }

    public boolean isAllowedSecondDeadline() {
        return allowSecondDeadline;
    }
}
