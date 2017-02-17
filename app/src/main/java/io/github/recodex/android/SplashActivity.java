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
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, NavigationDrawer.class);
        startActivity(intent);
        finish();
    }
}
