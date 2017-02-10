package com.feetsdk.android.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String SHARED_PATH = "shared";
    private static SharedPreferencesHelper instance;  
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public static final String FEET_TOKEN = "FEET_TOKEN";
    public static final String MOBILE_DATA = "MOBILE_DATA";
    public static final String MUSIC_NUMBER = "MUSIC_NUMBER";
    public static final String FAVORITE_NUMBER = "FAVORITE_NUMBER";
    public static final String USER_LEVEL = "USER_LEVEL";



    public static SharedPreferencesHelper getInstance(Context context) {
        if (instance == null && context != null) {  
            instance = new SharedPreferencesHelper(context);
            instance.putIntValue(MUSIC_NUMBER,10);
            instance.putIntValue(FAVORITE_NUMBER,10);
            instance.putStringValue(USER_LEVEL,"basic");
        }
        return instance;  
    }  
  
    private SharedPreferencesHelper(Context context) {
        sp = context.getSharedPreferences(SHARED_PATH, Context.MODE_PRIVATE);  
        editor = sp.edit();  
    }  
  
    public long getLongValue(String key) {  
        if (key != null && !key.equals("")) {  
            return sp.getLong(key, 0);  
        }  
        return 0;  
    }  
  
    public String getStringValue(String key) {  
        if (key != null && !key.equals("")) {  
            return sp.getString(key, null);  
        }  
        return null;  
    }  
  
    public int getIntValue(String key) {  
        if (key != null && !key.equals("")) {  
            return sp.getInt(key, 0);  
        }  
        return 0;  
    }


    public boolean isBasic(){
        return sp.getString(USER_LEVEL,"basic").equals("basic")?true:false;
    }
  
    public int getIntValueByDefault(String key)  
    {  
        if (key != null && !key.equals("")) {  
            return sp.getInt(key, 0);  
        }  
        return 0;  
    }  
    public boolean getBooleanValue(String key) {  
        if (key != null && !key.equals("")) {  
            return sp.getBoolean(key, false);  
        }  
        return true;  
    }  
  
    public float getFloatValue(String key) {  
        if (key != null && !key.equals("")) {  
            return sp.getFloat(key, 0);  
        }  
        return 0;  
    }  
  
    public void putStringValue(String key, String value) {  
        if (key != null && !key.equals("")) {  
            editor = sp.edit();  
            editor.putString(key, value);  
            editor.commit();  
        }  
    }  
  
    public void putIntValue(String key, int value) {  
        if (key != null && !key.equals("")) {  
            editor = sp.edit();  
            editor.putInt(key, value);  
            editor.commit();  
        }  
    }  
  
    public void putBooleanValue(String key, boolean value) {  
        if (key != null && !key.equals("")) {  
            editor = sp.edit();  
            editor.putBoolean(key, value);  
            editor.commit();  
        }  
    }  
  
    public void putLongValue(String key, long value) {  
        if (key != null && !key.equals("")) {  
            editor = sp.edit();  
            editor.putLong(key, value);  
            editor.commit();  
        }  
    }  
  
    public void putFloatValue(String key, Float value) {  
        if (key != null && !key.equals("")) {  
            editor = sp.edit();  
            editor.putFloat(key, value);  
            editor.commit();  
        }  
    }  
} 