package io.github.recodex.android.api;


import android.util.Log;

import java.io.IOException;

import io.github.recodex.android.users.UsersManager;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private final UsersManager usersManager;

    public TokenAuthenticator(UsersManager usersManager) {
        this.usersManager = usersManager;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if (response.request().url().encodedPathSegments().contains("login")) {
            return null;
        }

        Log.d("recodex", "TokenAuthenticator called");

        // Refresh your access_token using a synchronous api request
        usersManager.invalidateAuthToken();
        String newAccessToken = usersManager.blockingGetAuthToken();

        // Add new header to rejected request and retry it
        return response.request().newBuilder()
                .addHeader(Constants.authorizationHeader, Constants.tokenPrefix + newAccessToken)
                .build();
    }
}
