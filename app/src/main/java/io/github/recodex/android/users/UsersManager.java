package io.github.recodex.android.users;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import io.github.recodex.android.authentication.ReCodExAuthenticator;

public class UsersManager {

    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_LOGIN_TYPE = "LOGIN_TYPE";
    public static final String KEY_USERNAME = "USERNAME";

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final AccountManager accountManager;
    private UserWrapper currentUser;

    public UsersManager(Application application, AccountManager accountManager) {
        this.context = application.getBaseContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.accountManager = accountManager;

        loadSavedUser();
    }

    private void loadSavedUser() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        if (!TextUtils.isEmpty(username)) {
            Log.d("recodex", "Loaded username: " + username);

            for (Account account : getAvailableAccounts()) {
                if (account.name.equals(username)) {
                    String id = accountManager.getUserData(account, KEY_USER_ID);
                    String loginType = accountManager.getUserData(account, KEY_LOGIN_TYPE);
                    currentUser = new UserWrapper(context, id, account, loginType);
                    Log.d("recodex", "User successfully loaded from preferences");
                    break;
                }
            }
        }
    }

    public UserWrapper getCurrentUser() {
        return currentUser;
    }

    public String blockingGetAuthToken() throws IOException {
        if (currentUser == null) {
            return "";
        }

        String accessToken = "";
        try {
            accessToken = accountManager.blockingGetAuthToken(currentUser.getAccount(), ReCodExAuthenticator.AUTH_TOKEN_TYPE, true);
        } catch (OperationCanceledException | AuthenticatorException e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    public void invalidateAuthToken() {
        String token = accountManager.peekAuthToken(getCurrentUser().getAccount(), ReCodExAuthenticator.AUTH_TOKEN_TYPE);

        if (!TextUtils.isEmpty(token)) {
            accountManager.invalidateAuthToken(ReCodExAuthenticator.ACCOUNT_TYPE, token);
        }
    }

    public UserWrapper getUserForAccount(Account account) {
        for (Account acc : getAvailableAccounts()) {
            if (acc.equals(account)) {
                String id = accountManager.getUserData(account, KEY_USER_ID);
                String loginType = accountManager.getUserData(account, KEY_LOGIN_TYPE);
                return new UserWrapper(context, id, account, loginType);
            }
        }

        return null;
    }

    public UserWrapper switchCurrentUser(Account account) {
        if (currentUser != null && currentUser.getAccount().equals(account)) {
            return currentUser;
        }

        currentUser = getUserForAccount(account);

        // save user id into preferences
        sharedPreferences.edit().putString(KEY_USERNAME, account.name).apply();

        return currentUser;
    }

    public Account[] getAvailableAccounts() throws SecurityException {
        return accountManager.getAccountsByType(ReCodExAuthenticator.ACCOUNT_TYPE);
    }

    public UserWrapper addUserExplicitly(Account account, String accessToken, String id, String password, LoginType loginType) {
        // Creating the account on the device and setting the auth token we got
        accountManager.addAccountExplicitly(account, password, null);
        accountManager.setAuthToken(account, ReCodExAuthenticator.AUTH_TOKEN_TYPE, accessToken);

        // set user data
        accountManager.setUserData(account, KEY_USER_ID, id);
        accountManager.setUserData(account, KEY_LOGIN_TYPE, LoginType.typeToString(loginType));

        return switchCurrentUser(account);
    }

    public void addAccount(AppCompatActivity activity, AccountManagerCallback<Bundle> callback) {
        accountManager.addAccount(ReCodExAuthenticator.ACCOUNT_TYPE,
                ReCodExAuthenticator.AUTH_TOKEN_TYPE, null, null, activity, callback, null);
    }
}
