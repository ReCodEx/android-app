package io.github.recodex.android.api;


import android.content.SharedPreferences;

import java.io.IOException;

import io.github.recodex.android.R;
import io.github.recodex.android.utils.Utils;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {
    private SharedPreferences preferences;

    public TokenAuthenticator(SharedPreferences prefs) {
        preferences = prefs;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Refresh your access_token using a synchronous api request
        String newAccessToken = Utils.getApi().login("ps@stdin.cz", "").execute().body().getPayload().getAccessToken();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.tokenPrefsId, newAccessToken);
        editor.commit();

        // Add new header to rejected request and retry it
        return response.request().newBuilder()
                .addHeader(Constants.authorizationHeader, Constants.tokenPrefix + newAccessToken)
                .build();
    }
}
