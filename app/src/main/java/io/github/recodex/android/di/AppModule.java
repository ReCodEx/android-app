package io.github.recodex.android.di;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.recodex.android.R;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.api.TokenAuthenticator;
import io.github.recodex.android.api.TokenInterceptor;
import io.github.recodex.android.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by martin on 2/17/17.
 */

@Module
public class AppModule {

    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    private String getBaseUrl(SharedPreferences prefs) {
        String baseUrl = prefs.getString("api_uri", mApplication.getBaseContext().getString(R.string.pref_default_api_url));
        String version = prefs.getString("api_version", mApplication.getBaseContext().getString(R.string.pref_default_api_version));
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

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Singleton
    AccountManager providesAccountManager() {
        return AccountManager.get(mApplication.getBaseContext());
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(AccountManager accountManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(new TokenInterceptor(accountManager));
        builder.authenticator(new TokenAuthenticator(accountManager));
        OkHttpClient client = builder.build();
        return client;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient okHttpClient, SharedPreferences sharedPreferences) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getBaseUrl(sharedPreferences))
                .client(okHttpClient)
                .build();
        return retrofit;
    }

    @Provides
    @Singleton
    RecodexApi providesReCodExApi(Retrofit retrofit, SharedPreferences sharedPreferences) {
        return retrofit.create(RecodexApi.class);
    }
}
