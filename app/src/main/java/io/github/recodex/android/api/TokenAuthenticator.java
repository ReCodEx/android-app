package io.github.recodex.android.api;


import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;

import java.io.IOException;

import io.github.recodex.android.R;
import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.model.Login;
import io.github.recodex.android.utils.Utils;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    public TokenAuthenticator() {}

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Refresh your access_token using a synchronous api request
        String newAccessToken = null;
        try {
            newAccessToken = Utils.getAccountManager().blockingGetAuthToken(Utils.getCurrentAccount(), ReCodExAuthenticator.AUTH_TOKEN_TYPE, true);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }

        // Add new header to rejected request and retry it
        return response.request().newBuilder()
                .addHeader(Constants.authorizationHeader, Constants.tokenPrefix + newAccessToken)
                .build();
    }
}
