package io.github.recodex.android.users;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;

import io.github.recodex.android.R;
import io.github.recodex.android.api.Constants;
import io.github.recodex.android.model.Login;

/**
 * Created by martin on 2/17/17.
 */

public class UserWrapper {

    private static final String FULL_NAME = "FULL_NAME";
    private static final String AVATAR_URL = "AVATAR_URL";

    private Context context;
    private SharedPreferences preferences;
    private Account account;
    private String id;
    private LoginType loginType;

    public UserWrapper(Context context, String id, Account account, String loginType) {
        this.context = context;
        this.id = id;
        this.loginType = LoginType.stringToType(loginType);
        this.account = account;
        this.preferences = context.getSharedPreferences(context.getString(R.string.user_preferences_prefix) + id, Context.MODE_PRIVATE);
    }

    public Account getAccount() { return account; }

    public String getId() { return id; }

    public LoginType getLoginType() { return loginType; }

    public String getFullName() {
        return preferences.getString(FULL_NAME, context.getString(R.string.john_doe));
    }

    public String getAvatarUrl() {
        return preferences.getString(AVATAR_URL, "");
    }

    public void updateData(Login login) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FULL_NAME, login.getUser().getFullName());
        editor.putString(AVATAR_URL, login.getUser().getAvatarUrl());
        editor.commit();
    }
}
