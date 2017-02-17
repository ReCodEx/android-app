package io.github.recodex.android.api;


import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;
import android.media.session.MediaSession;

import java.io.IOException;

import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.utils.Utils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private AccountManager accountManager;

    public TokenInterceptor(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        if (Utils.getCurrentAccount() != null) {
            Request originalRequest = chain.request();

            String token = "";
            try {
                token = accountManager.blockingGetAuthToken(Utils.getCurrentAccount(), ReCodExAuthenticator.AUTH_TOKEN_TYPE, true);
            } catch (OperationCanceledException | AuthenticatorException e) {
                e.printStackTrace();
            }

            if (token.isEmpty() || alreadyHasAuthorizationHeader(originalRequest)) {
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
