package com.feetsdk.android;

import android.content.Context;

import com.feetsdk.android.feetsdk.annotation.PlayMusicType;
import com.feetsdk.android.feetsdk.download.DownloadControler;
import com.feetsdk.android.feetsdk.http.HttpControler;
import com.feetsdk.android.feetsdk.music.MusicHelper;
import com.feetsdk.android.feetsdk.player.PlayerControler;
import com.feetsdk.android.feetsdk.ui.FwController;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/11/9.
 *
 */
public class FeetSdk {

    private FeetConfig config;

    private static FeetSdk instance;

    private Context context;
    public synchronized static FeetSdk getInstance(Context context) {
        return instance == null?instance = new FeetSdk(context):instance;
    }


    private FeetSdk(Context context) {
        config = new FeetConfig(context);
        this.context = context;
    }

    public void init(){
        config.initDevInfo(context);
        init(config.getAppkey(), config.getAppChannel());
    }

    public void init(String appKey, String appChannel){
        if (config.checkSelf(appKey,appChannel)) {
//            return;
        }
        config.initDevInfo(context);
        config.setAppKey(appKey);
        config.setAppChannel(appChannel);
        config.initAppInfo(context);
        config.initDevInfo(context);
        config.debug("使用开始");
    }

    public void setMobileNetWorkVisiable(boolean netWorkVisiable){
        config.setCellularData(netWorkVisiable);
    }

    public void setMusicLibrarySize(int size){
        config.setDefaultMusciSize(size);
    }

    public static boolean getMobileNetWorkVisiable(){
        FeetConfig feetConfig = new FeetConfig();
        return feetConfig.getCellularData();
    }

    public static HttpControler getHttpControler(Context context) {
        return new HttpControler(context);
    }

    public static DownloadControler getDownloadControler(Context context) {
        return new DownloadControler(context);
    }

    public static PlayerControler getPlayerControler(Context context,@PlayMusicType int type){
        return new PlayerControler(context,type);
    }

    public static MusicHelper getMusicHelper(Context context){
        return new MusicHelper(new WeakReference<>(context));
    }

    public static FwController getFeetUiController(Context context){
        return  FwController.getInstance(context);
    }

}
