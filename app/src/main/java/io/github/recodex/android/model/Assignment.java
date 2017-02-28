package io.github.recodex.android.model;

import java.util.List;

public class Assignment {
    private String name;
    private String id;
    private List<AssignmentText> localizedTexts;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<AssignmentText> getLocalizedTexts() {
        return localizedTexts;
    }
}
