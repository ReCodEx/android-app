package io.github.recodex.android.users;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.model.Login;

/**
 * Created by martin on 2/17/17.
 */

public class UsersManager {

    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_USERNAME = "USERNAME";

    private Application application;
    private Context context;
    private SharedPreferences sharedPreferences;
    private AccountManager accountManager;
    private UserWrapper currentUser;

    public UsersManager(Application application, AccountManager accountManager) {
        this.application = application;
        this.context = application.getBaseContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.accountManager = accountManager;

        loadSavedUser();
    }

    private void loadSavedUser() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        Log.d("recodex", "Loaded username: " + username);

        if (!TextUtils.isEmpty(username)) {
            for (Account account : getAvailableAccounts()) {
                if (account.name.equals(username)) {
                    String id = accountManager.getUserData(account, KEY_USER_ID);
                    currentUser = new UserWrapper(context, id, account);
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

    public UserWrapper switchCurrentUser(Account account) {
        if (currentUser != null && currentUser.getAccount().equals(account)) {
            return currentUser;
        }

        currentUser = null;
        for (Account acc : getAvailableAccounts()) {
            if (acc.equals(account)) {
                String id = accountManager.getUserData(account, KEY_USER_ID);
                currentUser = new UserWrapper(context, id, account);

                // save user id into preferences
                sharedPreferences.edit().putString(KEY_USERNAME, account.name).apply();

                break;
            }
        }

        return currentUser;
    }

    public Account[] getAvailableAccounts() throws SecurityException {
        Account result[] = accountManager.getAccountsByType(ReCodExAuthenticator.ACCOUNT_TYPE);
        return result;
    }

    public UserWrapper addUserExplicitly(Account account, String accessToken, String id, String password) {
        // Creating the account on the device and setting the auth token we got
        accountManager.addAccountExplicitly(account, password, null);
        accountManager.setAuthToken(account, ReCodExAuthenticator.AUTH_TOKEN_TYPE, accessToken);

        // set user data
        accountManager.setUserData(account, KEY_USER_ID, id);

        return switchCurrentUser(account);
    }

    public void addAccount(Activity activity, AccountManagerCallback<Bundle> callback) {
        final AccountManagerFuture<Bundle> future =
                accountManager.addAccount(ReCodExAuthenticator.ACCOUNT_TYPE,
                        ReCodExAuthenticator.AUTH_TOKEN_TYPE, null, null, activity, callback, null);
    }
}