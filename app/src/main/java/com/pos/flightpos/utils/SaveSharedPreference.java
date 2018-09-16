package com.pos.flightpos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {

    static final String PREF_USER_NAME= "username";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static void setStringValues(Context ctx, String key,
                                       String DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(key, DataToSave);
        editor.commit();
    }

    public static String getStringValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, null);
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static void removeUserName(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(getSharedPreferences(ctx).getString(PREF_USER_NAME, ""));
        editor.commit();
    }

    public static void removeValue(Context ctx,String key){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(getSharedPreferences(ctx).getString(key, ""));
        editor.commit();
    }
}
