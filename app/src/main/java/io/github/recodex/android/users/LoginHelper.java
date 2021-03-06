package io.github.recodex.android.users;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import io.github.recodex.android.LoginActivity;
import io.github.recodex.android.R;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.Login;
import retrofit2.Response;

public class LoginHelper {

    public static final String KEY_LOGIN_RESULT = "LOGIN_RESULT";

    private final Context context;
    private final UsersManager usersManager;
    private final RecodexApi recodexApi;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    public LoginHelper(Application application, UsersManager usersManager, RecodexApi recodexApi) {
        this.context = application.getBaseContext();
        this.recodexApi = recodexApi;
        this.usersManager = usersManager;
    }

    public void attemptRegularLogin(LoginActivity activity, TextView mEmailView, TextView mPasswordView) {
        String email = mEmailView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(context.getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(context.getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        attemptLogin(activity, mEmailView, mPasswordView, LoginType.REGULAR);
    }

    public void attemptCasLogin(LoginActivity activity, TextView mEmailView, TextView mPasswordView) {
        attemptLogin(activity, mEmailView, mPasswordView, LoginType.CAS_UK);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(LoginActivity activity, TextView mEmailView, TextView mPasswordView, LoginType type) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(context.getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        activity.showProgress(true);
        mAuthTask = new UserLoginTask(activity, mEmailView, mPasswordView, type);
        mAuthTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Intent> {

        private final LoginActivity activity;
        private final TextView mEmailView;
        private final TextView mPasswordView;

        private final String mEmail;
        private final String mPassword;
        private final LoginType loginType;

        UserLoginTask(LoginActivity activity, TextView emailView, TextView passwordView, LoginType loginType) {
            this.activity = activity;
            mEmailView = emailView;
            mPasswordView = passwordView;

            this.mEmail = emailView.getText().toString();
            this.mPassword = passwordView.getText().toString();
            this.loginType = loginType;
        }

        @Override
        protected Intent doInBackground(Void... params) {
            Log.d(context.getString(R.string.recodex_log_tag), "Login on background...");
            Intent result = new Intent();

            try {
                Response<Envelope<Login>> response;
                if (loginType == LoginType.REGULAR) {
                    response = recodexApi.login(mEmail, mPassword).execute();
                } else {
                    response = recodexApi.externalLogin(LoginType.typeToString(loginType), mEmail, mPassword).execute();
                }

                if (!response.isSuccessful() || response.body().getCode() != 200) {
                    Log.d(context.getString(R.string.recodex_log_tag), "Response from server was not successful.");
                    result.putExtra(KEY_LOGIN_RESULT, false);
                } else {
                    Log.d(context.getString(R.string.recodex_log_tag), "Login acquired from server, saving...");
                    Login login = response.body().getPayload();

                    String accountType = activity.getIntent().getStringExtra(ReCodExAuthenticator.ARG_ACCOUNT_TYPE);

                    result.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
                    result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    result.putExtra(AccountManager.KEY_AUTHTOKEN, login.getAccessToken());
                    result.putExtra(AccountManager.KEY_PASSWORD, mPassword);
                    result.putExtra(KEY_LOGIN_RESULT, true);

                    // create account
                    final Account account = new Account(mEmail, accountType);
                    UserWrapper user = usersManager.addUserExplicitly(account, login.getAccessToken(), login.getUser().getId(), mPassword, loginType);

                    user.updateData(login);

                    // set sync adapter
                    ContentResolver.setIsSyncable(account, ReCodExAuthenticator.PROVIDER_AUTHORITY, 1);
                    ContentResolver.setSyncAutomatically(account, ReCodExAuthenticator.PROVIDER_AUTHORITY, true);
                }
            } catch (IOException e) {
                result.putExtra(KEY_LOGIN_RESULT, false);
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Intent intent) {
            mAuthTask = null;
            activity.showProgress(false);

            Log.d(context.getString(R.string.recodex_log_tag), "On post execute after login...");

            if (intent.getBooleanExtra(KEY_LOGIN_RESULT, false)) {
                Log.d(context.getString(R.string.recodex_log_tag), "Login successful...");

                activity.setAccountAuthenticatorResult(intent.getExtras());
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            } else {
                Log.d(context.getString(R.string.recodex_log_tag), "Login unsuccessful...");

                mPasswordView.setError(context.getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            activity.showProgress(false);
        }
    }
}
