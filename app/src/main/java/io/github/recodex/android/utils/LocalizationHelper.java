package io.github.recodex.android.utils;

import java.util.List;

import io.github.recodex.android.model.LocalizedText;
import io.github.recodex.android.users.UsersManager;

public class LocalizationHelper {

    private final static String DEFAULT_LOCALE = "en";

    private UsersManager users;

    public LocalizationHelper(UsersManager users) {
        this.users = users;
    }


    /**
     * For given texts pick up the text which corresponds to the current user locale.
     * @param texts texts which will be iterated over
     * @param <T> localized texts can be of various types
     * @return chosen localized text
     */
    public <T extends LocalizedText> T getUserLocalizedText(List<T> texts) {
        if (texts.isEmpty()) {
            return null;
        }

        String userLocale = DEFAULT_LOCALE;
        if (users.getCurrentUser() != null) {
            userLocale = users.getCurrentUser().getDefaultLanguage();
        }

        for (T text : texts) {
            if (userLocale.equals(text.getLocale())) {
                return text;
            }
        }

        return texts.get(0);
    }
}
