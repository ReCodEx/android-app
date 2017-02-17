package io.github.recodex.android.utils;


import android.accounts.Account;
import android.accounts.AccountManager;

import io.github.recodex.android.api.RecodexApi;

public class Utils {
    private static Account currentAccount = null;

    public static void setCurrentAccount(Account a) {
        currentAccount = a;
    }

    public static Account getCurrentAccount() { return currentAccount; }
}
