package io.github.recodex.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import io.github.recodex.android.MyApp;
import io.github.recodex.android.R;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.users.UserWrapper;
import io.github.recodex.android.users.UsersManager;

public class ReCodExSyncAdapter extends AbstractThreadedSyncAdapter {
    @Inject
    AccountManager accountManager;
    @Inject
    ApiDataFetcher userDataFetcher;
    @Inject
    UsersManager usersManager;

    public ReCodExSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        // Dagger DI
        ((MyApp) getContext().getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(getContext().getString(R.string.recodex_log_tag), "onPerformSync for account: " + account.name);
        try {
            // Get the auth token for the current account
            String authToken = accountManager.blockingGetAuthToken(account, ReCodExAuthenticator.AUTH_TOKEN_TYPE, true);

            // get user for given account and fetch all data
            UserWrapper user = usersManager.getUserForAccount(account);
            userDataFetcher.fetchAndStoreAll(user);

            Log.d(getContext().getString(R.string.recodex_log_tag), "onPerformSync successful");
        } catch (Exception e) {
            Log.d(getContext().getString(R.string.recodex_log_tag), "Error onPerformSync: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
