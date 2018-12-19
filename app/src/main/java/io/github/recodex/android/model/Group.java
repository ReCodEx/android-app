package io.github.recodex.android.model;

import java.util.List;

public class Group {
    private String id;
    private List<LocalizedGroup> localizedTexts;
    private GroupPrivateData privateData;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return privateData.getDescription();
    }

    public List<LocalizedGroup> getLocalizedTexts() {
        return localizedTexts;
    }

    public GroupPrivateData getPrivateData() {
        return privateData;
    }
}
