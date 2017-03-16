package io.github.recodex.android.api;


import android.util.Log;

import java.io.IOException;

import io.github.recodex.android.users.UsersManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private UsersManager usersManager;

    public TokenInterceptor(UsersManager usersManager) {
        this.usersManager = usersManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        if (chain.request().url().encodedPathSegments().contains("login")) {
            return chain.proceed(chain.request());
        }

        if (usersManager.getCurrentUser() != null) {
            Log.d("recodex", "TokenInterceptor called");

            Request originalRequest = chain.request();

            String token = usersManager.blockingGetAuthToken();

            if (token.isEmpty() || alreadyHasAuthorizationHeader(originalRequest)) {
                Log.d("recodex", "TokenInterceptor - token empty");
                return chain.proceed(originalRequest);
            }

            // Add authorization header with updated authorization value to intercepted request
            Request authorisedRequest = originalRequest.newBuilder()
                    .header(Constants.authorizationHeader, Constants.tokenPrefix + token)
                    .build();
            return chain.proceed(authorisedRequest);
        }

        return chain.proceed(chain.request());
    }

    private boolean alreadyHasAuthorizationHeader(Request request) {
        return request.headers().names().contains(Constants.authorizationHeader);
    }
}
