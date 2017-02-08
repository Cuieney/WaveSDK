package com.feetsdk.android.feetsdk.http;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by cuieney on 16/11/10.
 */
interface ApiInterface {
    String BASE_URL = "https://wave.paohaile.com";
    String GET_ARTISTS = BASE_URL+"/v1/radio/artists?page-size=30&page-no=1";
    String SEARCH_ARTISTS = BASE_URL+"/v1/radio/artists?page-size=30&page-no=1&q=";
    String GET_MUSIC = BASE_URL+"/v1/radio?page-size=";
    String DELETE_FAVORITE = BASE_URL+"/v1/favorites?song-id=";
    String GET_FAVORITES = BASE_URL+"/v1/favorites?page-size=";
    String POST_FAVORITES = BASE_URL+"/v1/favorites";
    String POST_ARTISTS = BASE_URL+"/v1/radio/artists";
    String POST_RUNLOG = BASE_URL+"/v1/run-log";
    String GET_USER_INFO = BASE_URL+"/v1/auth/me";
    String api = "http://songcheck.paohaile.com:8030/temporecords";


    
    void getArtists(IHttpRspCallBack callBack);

    void searchArtists(IHttpRspCallBack callBack,String artistName);

    void postArtists(@NonNull String artists1, @NonNull String artists2, @NonNull String artists3,IHttpRspCallBack callBack);

    void getMusic(int songNum,IHttpRspCallBack callBack);

    void getFavorites(IHttpRspCallBack callBack);

    void postFavorites(IHttpRspCallBack callBack,String... musicId);

    void deleteFavorite(@NonNull String musicId,IHttpRspCallBack callBack);

    void postRunLog(JSONObject resRunLog, IHttpRspCallBack callBack);

    void getUserInfo(IHttpRspCallBack callBack);

}
