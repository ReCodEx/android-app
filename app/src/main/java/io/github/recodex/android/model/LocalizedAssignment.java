package io.github.recodex.android.model;

public class LocalizedAssignment extends LocalizedText {
    private String id;
    private String name;
    private String text;
    private int createdAt;
    private String createdFrom;

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
