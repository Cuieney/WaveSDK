package com.feetsdk.android.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/8/25.
 */
public class BaseAppPreference {
    protected static BaseAppPreference INSTANCE;


    protected static final String PREF_TOKEN = "token";
    protected static final String PREFERENCE = "app_preference";

    private WeakReference<Context> contextWeakReference;

    public BaseAppPreference(WeakReference<Context> contextWeakReference) {
        this.contextWeakReference = contextWeakReference;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        setString(PREF_TOKEN, token);
        this.token = token;
    }

    protected String token;


    public static BaseAppPreference getInstance(Context context) {
        if (null == INSTANCE) {
            INSTANCE = new BaseAppPreference(new WeakReference<Context>(context));
        }
        return INSTANCE;
    }

    protected SharedPreferences.Editor editor() {
        return preferences().edit();
    }

    protected SharedPreferences preferences() {


        if(contextWeakReference.get() !=null){

            return contextWeakReference.get().getSharedPreferences(PREFERENCE, contextWeakReference.get().MODE_PRIVATE);
        }

        return null;

    }

    public void setFloat(String key, float values) {

        editor().putFloat(key, values).commit();
    }

    public float getFloat(String key, float defValue) {

        SharedPreferences prefer= preferences();

        if(prefer!=null){
            return prefer.getFloat(key, defValue);
        }
        return 0;
    }

    public void setLong(String key, Long values) {

        editor().putLong(key, values).commit();
    }

    public Long getLong(String key, Long defValue) {

        SharedPreferences prefer= preferences();
        if(prefer!=null){
            return prefer.getLong(key, defValue);
        }
        return 0l;
    }


    public void setString(String key, String values) {

        editor().putString(key, values).commit();
    }

    public void setInt(String key, int values) {
        editor().putInt(key, values).commit();
    }

    public void setBoolean(String key, boolean values) {
        editor().putBoolean(key, values).commit();
    }

    public int getInt(String key, int defValue) {

        SharedPreferences prefer= preferences();

        if(prefer!=null){

            return prefer.getInt(key, defValue);
        }

        return 0;
    }


    public String getString(String key, String defValue) {

        SharedPreferences prefer= preferences();


        if(prefer!=null){

            return prefer.getString(key, defValue);
        }

        return null;
    }

    public boolean getBoolean(String key, boolean defValue) {

        SharedPreferences prefer=preferences();


        if(prefer!=null){

            return prefer.getBoolean(key, defValue);
        }

        return false;
    }
}
