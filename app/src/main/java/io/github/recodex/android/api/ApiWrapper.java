package io.github.recodex.android.api;

import android.app.Application;
import android.content.SharedPreferences;

import java.io.IOException;

import androidx.annotation.NonNull;
import io.github.recodex.android.R;
import io.github.recodex.android.users.UsersManager;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiWrapper<T> {

    private final T cacheOnlyApi;
    private final T realApi;

    public ApiWrapper(Class<T> cls, Application application, UsersManager usersManager, SharedPreferences preferences) {
        Cache cache = new Cache(application.getCacheDir(), 10 * 1024 * 1024);
        OkHttpClient cacheOnlyClient = createHttpClientBuilder(usersManager)
                .addInterceptor(new ForcedCacheInterceptor())
                .cache(cache)
                .build();
        cacheOnlyApi = createApi(cls, application, preferences, cacheOnlyClient);

        OkHttpClient realClient = createHttpClientBuilder(usersManager)
                .cache(cache)
                .build();
        realApi = createApi(cls, application, preferences, realClient);
    }

    public T fromCache() {
        return cacheOnlyApi;
    }

    public T fromRemote() {
        return realApi;
    }

    private T createApi(Class<T> cls, Application application, SharedPreferences preferences, OkHttpClient client) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getBaseUrl(application, preferences))
                .client(client)
                .build()
                .create(cls);
    }

    private String getBaseUrl(Application application, SharedPreferences prefs) {
        String baseUrl = prefs.getString("api_uri", application.getBaseContext().getString(R.string.pref_default_api_url));
        String version = prefs.getString("api_version", application.getBaseContext().getString(R.string.pref_default_api_version));
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

    private OkHttpClient.Builder createHttpClientBuilder(UsersManager usersManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        builder.addInterceptor(new TokenInterceptor(usersManager));
        builder.authenticator(new TokenAuthenticator(usersManager));
        return builder;
    }
}

class ForcedCacheInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build();
        return chain.proceed(request);
    }
}
