package io.github.recodex.android.utils;


import android.accounts.Account;
import android.accounts.AccountManager;

import io.github.recodex.android.api.RecodexApi;

public class Utils {
    private static RecodexApi api = null;
    private static AccountManager accountManager = null;
    private static Account currentAccount = null;


    public static void setApi(RecodexApi r) {
        api = r;
    }

    public static RecodexApi getApi() {
        return api;
    }

    public static void setAccountManager(AccountManager am) {
        accountManager = am;
    }

    public static AccountManager getAccountManager() {
        return accountManager;
    }

    public static void setCurrentAccount(Account a) {
        currentAccount = a;
    }

    public static Account getCurrentAccount() { return currentAccount; }
}
