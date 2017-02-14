package io.github.recodex.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

        Intent intent = new Intent(this, NavigationDrawer.class);
        startActivity(intent);
        finish();
    }

    private void initApiClient() {
        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(new TokenInterceptor(getPreferences(MODE_PRIVATE)));
        builder.authenticator(new TokenAuthenticator(getPreferences(MODE_PRIVATE)));
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RecodexApi api = retrofit.create(RecodexApi.class);
        Utils.setApi(api);
    }

    private String getBaseUrl() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
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
