package io.github.recodex.android.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import javax.inject.Inject;

import io.github.recodex.android.LoginActivity;
import io.github.recodex.android.MyApp;
import io.github.recodex.android.R;
import io.github.recodex.android.api.Constants;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.utils.Utils;
import retrofit2.Response;


public class ReCodExAuthenticator extends AbstractAccountAuthenticator {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String ACCOUNT_TYPE = "io.github.recodex.android.authentication";

    public static final String KEY_USER_ID = "USER_ID";

    public final static String AUTH_TOKEN_TYPE = "full";
    public final static String AUTH_TOKEN_TYPE_LABEL = "Full access";

    private final Context mContext;

    @Inject
    RecodexApi recodexApi;

    public ReCodExAuthenticator(Context context) {
        super(context);

        mContext = context;
        ((MyApp) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
        Log.d(mContext.getString(R.string.recodex_log_tag),"*** addAccount method called");

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra(ARG_ACCOUNT_TYPE, s);
        intent.putExtra(ARG_AUTH_TYPE, s1);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);

        final Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        Log.d(mContext.getString(R.string.recodex_log_tag), "*** getAuthToken method called");

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AUTH_TOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);
        String authToken = am.peekAuthToken(account, authTokenType);

        Log.d(mContext.getString(R.string.recodex_log_tag), "peekAuthToken returned - " + authToken);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    Log.d(mContext.getString(R.string.recodex_log_tag), "*** re-authenticating with the existing password");

                    Response<Envelope<Login>> response = recodexApi.login(account.name, password).execute();
                    if (response.isSuccessful() && response.body().getCode() == 200) {
                        Login login = response.body().getPayload();
                        authToken = login.getAccessToken();

                        // save new user data
                        SharedPreferences prefs = mContext
                                .getSharedPreferences(mContext.getString(R.string.user_preferences_prefix) + login.getUser().getId(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Constants.userFullName, login.getUser().getFullName());
                        editor.putString(Constants.userAvatarUrl, login.getUser().getAvatarUrl());
                        editor.commit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        final Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return AUTH_TOKEN_TYPE_LABEL;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }
}
