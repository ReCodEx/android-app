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
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.model.Envelope;
import io.github.recodex.android.model.UserGroups;
import io.github.recodex.android.users.UserWrapper;
import io.github.recodex.android.users.UsersManager;
import retrofit2.Response;

/**
 * Created by martin on 2/25/17.
 */

public class ReCodExSyncAdapter extends AbstractThreadedSyncAdapter {
    @Inject
    AccountManager accountManager;
    @Inject
    RecodexApi recodexApi;
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

            // get user for given account
            UserWrapper user = usersManager.getUserForAccount(account);

            // get user groups and save them
            Response<Envelope<UserGroups>> response = recodexApi.getGroupsForUser(user.getId()).execute();
            UserGroups userGroups = response.body().getPayload();
            user.setGroupsInfo(userGroups.getStudent(), userGroups.getStats());

            Log.d(getContext().getString(R.string.recodex_log_tag), "onPerformSync successful");
        } catch (Exception e) {
            Log.d(getContext().getString(R.string.recodex_log_tag), "Error onPerformSync: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
