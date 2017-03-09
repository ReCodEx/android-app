package io.github.recodex.android;

import android.app.Application;

import io.github.recodex.android.di.AppComponent;
import io.github.recodex.android.di.AppModule;
import io.github.recodex.android.di.DaggerAppComponent;


public class MyApp extends Application {

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Dagger%COMPONENT_NAME%
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}