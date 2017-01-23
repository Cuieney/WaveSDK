package com.feetsdk.android.feetsdk.http;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by cuieney on 16/11/10.
 */
public class HttpControler implements ApiInterface{


    public static HttpControler singleton;
    private static HttpProxy httpProxy;
    public static HttpControler with(Context context){
        if (singleton == null) {
            synchronized (HttpProxy.class){
                singleton = new HttpControler();
                httpProxy = new HttpProxy(context);
            }
        }
        return singleton;
    }

    public HttpControler() {
    }

    public HttpControler(Context context) {
        httpProxy = new HttpProxy(context);
    }

    @Override
    public void getArtists(IHttpRspCallBack callBack) {
        httpProxy.getArtists(callBack);
    }

    @Override
    public void searchArtists(IHttpRspCallBack callBack, String artistName) {
        httpProxy.searchArtists(callBack,artistName);
    }

    @Override
    public void postArtists(@NonNull String artists1, @NonNull String artists2, @NonNull String artists3, IHttpRspCallBack callBack) {
        httpProxy.postArtists(artists1,artists2,artists3,callBack);
    }

    @Override
    public void getMusic(int songNum, IHttpRspCallBack callBack) {
        httpProxy.getMusic(songNum,callBack);
    }

    @Override
    public void getFavorites(IHttpRspCallBack callBack) {
        httpProxy.getFavorites(callBack);
    }

    @Override
    public void postFavorites(IHttpRspCallBack callBack, String... musicId) {
        httpProxy.postFavorites(callBack,musicId);
    }

    @Override
    public void deleteFavorite(@NonNull String musicId, IHttpRspCallBack callBack) {
        httpProxy.deleteFavorite(musicId,callBack);
    }

    @Override
    public void postRunLog(JSONObject resRunLog, IHttpRspCallBack callBack) {
        httpProxy.postRunLog(resRunLog,callBack);
    }

    @Override
    public void getUserInfo(IHttpRspCallBack callBack) {
        httpProxy.getUserInfo(callBack);
    }
}
