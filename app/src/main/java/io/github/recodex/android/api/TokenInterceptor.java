package io.github.recodex.android.api;


import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {
    private SharedPreferences preferences;

    public TokenInterceptor(SharedPreferences prefs) {
        preferences = prefs;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = getToken();

        if (token.isEmpty() || alreadyHasAuthorizationHeader(originalRequest)) {
            return chain.proceed(originalRequest);
        }

        // Add authorization header with updated authorization value to intercepted request
        Request authorisedRequest = originalRequest.newBuilder()
                .header(Constants.authorizationHeader, Constants.tokenPrefix + token)
                .build();
        return chain.proceed(authorisedRequest);
    }

    private String getToken() {
        return preferences.getString(Constants.tokenPrefsId, "");
    }

    private boolean alreadyHasAuthorizationHeader(Request request) {
        return request.headers().names().contains(Constants.authorizationHeader);
    }
}
