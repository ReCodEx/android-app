package io.github.recodex.android.utils;

import android.icu.text.DateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.recodex.android.model.LocalizedText;
import io.github.recodex.android.users.UsersManager;

public class LocalizationHelper {

    private final static String DEFAULT_LOCALE = "en";

    private final UsersManager users;

    public LocalizationHelper(UsersManager users) {
        this.users = users;
    }


    private String getUserLocale() {
        String locale = DEFAULT_LOCALE;
        if (users.getCurrentUser() != null) {
            locale = users.getCurrentUser().getDefaultLanguage();
        }

        return locale;
    }

    /**
     * For given texts pick up the text which corresponds to the current user locale.
     *
     * @param texts texts which will be iterated over
     * @param <T>   localized texts can be of various types
     * @return chosen localized text
     */
    public <T extends LocalizedText> T getUserLocalizedText(List<T> texts) {
        if (texts.isEmpty()) {
            return null;
        }

        String userLocale = getUserLocale();
        for (T text : texts) {
            if (userLocale.equals(text.getLocale())) {
                return text;
            }
        }

        return texts.get(0);
    }

    public String getDateTime(long datetime) {
        String userLocale = getUserLocale();
        Locale locale = Locale.forLanguageTag(userLocale);
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        return format.format(new Date(datetime * 1000));
    }
}
