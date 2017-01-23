package com.feetsdk.android.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by cuieney on 16/11/9.
 */
public class ManifestUtil {

    public static final String WEIXIN_REDIRECTURI_KEY = "weixin_redirecturi";
    public static final String FEET_APPKEY = "FEET_APPKEY";
    public static final String FEET_CHANNEL = "FEET_CHANNEL";


    /**
     * 读取微信的重定向URI
     * @param context context
     * @return String
     */
    public static String getWeixinRedirecturi(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if(null != appInfo) {
                Bundle bundle = appInfo.metaData;
                String url = null;
                if(null != bundle) {
                    url = bundle.getString(WEIXIN_REDIRECTURI_KEY);
                }

                if(!TextUtils.isEmpty(url)) {
                    return url;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 读取FEET的AppKey
     * @param context context
     * @return String
     */
    public static String getFeetAppkey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);

            if(null != appInfo) {
                Bundle bundle = appInfo.metaData;
                String key = null;
                if(null != bundle) {
                    key = String.valueOf(bundle.get(FEET_APPKEY));
                }
                if(!TextUtils.isEmpty(key)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 读取FEET的CHANNEL
     * @param context context
     * @return String
     */
    public static String getFeetChannel(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);

            if(null != appInfo) {
                Bundle bundle = appInfo.metaData;
                String key = null;
                if(null != bundle) {
                    key = String.valueOf(bundle.get(FEET_CHANNEL));
                }
                if(!TextUtils.isEmpty(key)) {
                    return key;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
