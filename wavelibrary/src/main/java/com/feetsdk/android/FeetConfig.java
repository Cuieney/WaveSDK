package com.feetsdk.android;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.feetsdk.android.common.utils.LocalPathResolver;
import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.common.utils.ManifestUtil;
import com.feetsdk.android.common.utils.SharedPreferencesHelper;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.io.File;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


/**
 * Created by cuieney on 16/11/9.
 */
public class FeetConfig implements IFeetConfig {


    private Context context;
    //初始化时间
    private static int initTime;
    //保存使用时间
    private int currentTime;


    static {
        initTime = 1 * 60 * 60;
    }

    public FeetConfig(Context context) {
        this.context = context;
        //初始化下载器配置
        FileDownloader.init(context,
                new FileDownloadHelper.OkHttpClientCustomMaker() { // is not has to provide.
                    @Override
                    public OkHttpClient customMake() {
                        // just for OkHttpClient customize.
                        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        // you can set the connection timeout.
                        builder.connectTimeout(15_000, TimeUnit.MILLISECONDS);
                        // you can set the HTTP proxy.
                        builder.proxy(Proxy.NO_PROXY);
                        // etc.
                        return builder.build();
                    }
                });

        LocalPathResolver.init(getBaseDir());
    }

    private String getBaseDir() {
        String base;
        if (ExistSDCard()) {
            base = Environment.getExternalStorageDirectory().getPath();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                base = context.getExternalCacheDir().getPath();
            } else {
                File filesDir = context.getApplicationContext().getFilesDir();
                base = filesDir.getPath();
            }
        }
        return base;
    }

    private boolean ExistSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * 开启使用计时
     */
    private void startTiming() {
        currentTime++;
    }

    public FeetConfig() {
    }

    @Override
    public void initDevInfo(Context context) {
        this.context = context;
    }

    @Override
    public String getAppkey() {

        String feet_token = SharedPreferencesHelper.getInstance(context).getStringValue(SharedPreferencesHelper.FEET_TOKEN);
        Logger.d("feet token " + feet_token);
        return feet_token;
    }

    @Override
    public String getAppChannel() {
        return ManifestUtil.getFeetChannel(context);
    }

    @Override
    public int getRestTime() {
        return initTime - currentTime;
    }

    @Override
    public boolean isTimeOut() {

        return initTime - currentTime >= 0;
    }

    @Override
    public void setAppKey(String appKey) {
        if (!TextUtils.isEmpty(appKey)) {
            SharedPreferencesHelper.getInstance(context).putStringValue(SharedPreferencesHelper.FEET_TOKEN, appKey);
        }
    }

    @Override
    public void setAppChannel(String appChannel) {

    }


    @Override
    public boolean checkSelf(String appKey, String appChannel) {
        if (TextUtils.isEmpty(appKey)) {
            return false;
        }
        return appKey.equals("abd");
    }

    @Override
    public void initAppInfo(Context context) {

    }

    public int getDefaultMusciSize() {
        return SharedPreferencesHelper.getInstance(context).getIntValue(SharedPreferencesHelper.MUSIC_NUMBER);
    }

    public void setDefaultMusciSize(int size) {
        checkUser(size);
    }

    public int getDefaultFavMusciSize() {
        return SharedPreferencesHelper.getInstance(context).getIntValue(SharedPreferencesHelper.FAVORITE_NUMBER);
    }

    public void checkUser(int size) {
        if (SharedPreferencesHelper.getInstance(context).isBasic()) {
            String txt = "免费用户不能设置曲库大小";
            Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
            return;
        } else {
            SharedPreferencesHelper.getInstance(context).putIntValue(SharedPreferencesHelper.MUSIC_NUMBER, size);
            SharedPreferencesHelper.getInstance(context).putIntValue(SharedPreferencesHelper.FAVORITE_NUMBER, 40);
        }
    }

    public void setDefaultFavMusciSize(int size) {
        checkUser(size);
    }

    @Override
    public synchronized void setCellularData(boolean isOpen) {

        SharedPreferencesHelper.getInstance(context).putBooleanValue(SharedPreferencesHelper.MOBILE_DATA, isOpen);
    }

    @Override
    public synchronized boolean getCellularData() {
        return SharedPreferencesHelper.getInstance(context).getBooleanValue(SharedPreferencesHelper.MOBILE_DATA);
    }


    @Override
    public void debug(String debug) {
        Log.i("Config", "debug: " + debug);
    }


}
