package io.github.recodex.android;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import io.github.recodex.android.api.Constants;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.api.TokenAuthenticator;
import io.github.recodex.android.api.TokenInterceptor;
import io.github.recodex.android.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initApiClient();

        // init account manager
        Utils.setAccountManager(AccountManager.get(getBaseContext()));

        Intent intent = new Intent(this, NavigationDrawer.class);
        startActivity(intent);
        finish();
    }

    private void initApiClient() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(new TokenInterceptor());
        builder.authenticator(new TokenAuthenticator());
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl(prefs))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RecodexApi api = retrofit.create(RecodexApi.class);
        Utils.setApi(api);
    }

    private String getBaseUrl(SharedPreferences prefs) {
        String baseUrl = prefs.getString("api_uri", getString(R.string.pref_default_api_url));
        String version = prefs.getString("api_version", getString(R.string.pref_default_api_version));
        StringBuilder result = new StringBuilder();
        result.append(baseUrl);
        if (!baseUrl.endsWith("/")) {
            result.append('/');
        }
        result.append(version);
        if (!version.endsWith("/")) {
            result.append('/');
        }
        return result.toString();
    }
}
