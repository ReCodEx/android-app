package io.github.recodex.android.di;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.recodex.android.api.ApiWrapper;
import io.github.recodex.android.api.RecodexApi;
import io.github.recodex.android.users.ApiDataFetcher;
import io.github.recodex.android.users.LoginHelper;
import io.github.recodex.android.users.UsersManager;

/**
 * Created by martin on 2/17/17.
 */

@Module
public class AppModule {

    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
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
    ApiWrapper<RecodexApi> provideRecodexApiWrapper(Application application, UsersManager usersManager, SharedPreferences preferences) {
        return new ApiWrapper<>(RecodexApi.class, application, usersManager, preferences);
    }

    @Provides
    @Singleton
    RecodexApi provideRecodexApi(ApiWrapper<RecodexApi> wrapper) {
        return wrapper.fromRemote();
    }

    @Provides
    @Singleton
    UsersManager providesUsersManager(Application application, AccountManager accountManager) {
        return new UsersManager(application, accountManager);
    }

    @Provides
    @Singleton
    LoginHelper providesLoginHelper(UsersManager users, ApiWrapper<RecodexApi> api) {
        return new LoginHelper(mApplication, users, api.fromRemote());
    }

    @Provides
    @Singleton
    ApiDataFetcher providesApiDataFetcher(ApiWrapper<RecodexApi> api) {
        return new ApiDataFetcher(api.fromRemote(), api);
    }
}
