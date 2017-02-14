package io.github.recodex.android.utils;


import io.github.recodex.android.api.RecodexApi;

public class Utils {
    private static RecodexApi api = null;

    public static void setApi(RecodexApi r) {
        api = r;
    }

    public static RecodexApi getApi() {
        return api;
    }
}
