package io.github.recodex.android.model;


public class UserSettings {
    private boolean darkTheme;
    private boolean vimMode;
    private String defaultLanguage;
    private boolean openedSidebar;
    private boolean useGravatar;

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public boolean isVimMode() {
        return vimMode;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean isOpenedSidebar() {
        return openedSidebar;
    }

    public boolean isUseGravatar() {
        return useGravatar;
    }
}
