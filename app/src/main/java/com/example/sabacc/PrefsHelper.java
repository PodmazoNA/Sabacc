package com.example.sabacc;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    private static final String PREFS_NAME = "app_prefs";
    private static final String FIRST_RUN_KEY = "first_run";
    private static final String IS_LOGGED_IN_KEY = "is_logged_in";

    public static boolean isFirstRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(FIRST_RUN_KEY, true);
    }

    public static void setFirstRun(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(FIRST_RUN_KEY, value).apply();
    }

    public static boolean isLoggedIn(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(IS_LOGGED_IN_KEY, false);
    }

    public static void setLoggedIn(Context context, boolean value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(IS_LOGGED_IN_KEY, value).apply();
    }
}