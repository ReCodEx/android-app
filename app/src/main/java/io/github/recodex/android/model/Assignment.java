package io.github.recodex.android.model;

import java.util.List;

public class Assignment {
    private String name;
    private String id;
    private List<AssignmentText> localizedTexts;
    private long firstDeadline;
    private long secondDeadline;
    private boolean allowSecondDeadline;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<AssignmentText> getLocalizedTexts() {
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
