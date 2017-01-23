package com.feetsdk.android.feetsdk.http;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.feetsdk.android.FeetConfig;
import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.common.utils.SharedPreferencesHelper;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.db.OrmHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/11/11.
 */
class HttpProxy implements ApiInterface {

//    public static HttpProxy singleton;
//    private Context context;
//    public static HttpProxy with(Context context){
//        if (singleton == null) {
//            synchronized (HttpProxy.class){
//                singleton = new HttpProxy();
//            }
//        }
//        return singleton;
//    }

    private OrmHelper ormHelper;
    private WeakReference<Context> contextWeakReference;
    private NetWork netWork;
    private FeetConfig config;

    public HttpProxy(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
        config = new FeetConfig(context);
        netWork = new NetWork(context);
        ormHelper = new OrmHelper(contextWeakReference.get());
    }

    private Context getContext() {
        return contextWeakReference.get();
    }

    @Override
    public void getArtists(final IHttpRspCallBack callBack) {

        netWork.newBuilder(ApiInterface.GET_ARTISTS, EventType.METHOD_GET).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });

    }

    @Override
    public void searchArtists(final IHttpRspCallBack callBack, String artistName) {
        netWork.newBuilder(ApiInterface.SEARCH_ARTISTS+artistName, EventType.METHOD_GET).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });

    }

    @Override
    public void postArtists(@NonNull String artists1, @NonNull String artists2, @NonNull String artists3, final IHttpRspCallBack callBack) {
        artists = new ArrayList<>();
        artists.add(artists1);
        artists.add(artists2);
        artists.add(artists3);
        netWork.newBuilder(ApiInterface.POST_ARTISTS, EventType.METHOD_POST).build()
                .setPost(HttpRequestBody.with(getContext()).provideJsonBody(provideArtistJsonArray()))
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                        ormHelper.deletePrivateSong();
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });
    }

    @Override
    public void getMusic(int songNum, final IHttpRspCallBack callBack) {
        netWork.newBuilder(ApiInterface.GET_MUSIC + songNum, EventType.METHOD_GET).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });

    }

    @Override
    public void getFavorites(final IHttpRspCallBack callBack) {
        netWork.newBuilder(ApiInterface.GET_FAVORITES+config.getDefaultFavMusciSize(), EventType.METHOD_GET).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });

    }

    @Override
    public void postFavorites(final IHttpRspCallBack callBack, String... musicId) {
        int size = config.getDefaultFavMusciSize();
        if (ormHelper.queryAllFavMsc().size() >= size) {
            Toast.makeText(contextWeakReference.get(), "免费用户只能收藏"+ size +"首单曲", Toast.LENGTH_SHORT).show();
            return;
        }


        favorites = new ArrayList<>();
        for (int i = 0; i < musicId.length; i++) {
            favorites.add(musicId[i]);
        }

        netWork.newBuilder(ApiInterface.POST_FAVORITES, EventType.METHOD_POST).build()
                .setPost(HttpRequestBody.with(getContext()).provideJsonBody(provideFavoritesJsonArray()))
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);

                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });
    }

    @Override
    public void deleteFavorite(@NonNull String musicId, final IHttpRspCallBack callBack) {
        netWork.newBuilder(ApiInterface.DELETE_FAVORITE + musicId, EventType.METHOD_DELETE).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });
    }

    @Override
    public void postRunLog(JSONObject runLog, final IHttpRspCallBack callBack) {
        netWork.newBuilder(ApiInterface.POST_RUNLOG, EventType.METHOD_POST).build()
                .setPost(HttpRequestBody.with(getContext()).provideJsonBody(runLog.toString()))
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });


    }

    @Override
    public void getUserInfo(final IHttpRspCallBack callBack) {
        netWork.newBuilder(ApiInterface.GET_USER_INFO,EventType.METHOD_GET).build()
                .enqueue(new HttpCallBack() {
                    @Override
                    public void response(HttpResponse response) {
                        notification(response);
                        try {
                            JSONObject jo = new JSONObject(response.getMessage());
                            JSONObject membership = jo.getJSONObject("membership");
                            String level = membership.getString("level");
                            SharedPreferencesHelper.getInstance(contextWeakReference.get())
                                    .putStringValue(SharedPreferencesHelper.USER_LEVEL,level);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callBack.success(response);
                    }

                    @Override
                    public void failed(final HttpException exception) {
                        callBack.failed(exception);
                    }
                });
    }

    private void notification(HttpResponse response) {
        if (response.getCode() == 401) {
            if (contextWeakReference != null) {
                Looper.prepare();
                Toast.makeText(contextWeakReference.get(),"当前用户已在别处登录，请重新登录",Toast.LENGTH_LONG).show();
                Looper.loop();
            }else{
                Logger.d("null");
            }

        }
    }


    private List<String> artists;

    private String provideArtistJsonArray() {
        StringBuilder json = new StringBuilder();
        json.append("{\n" + "  \"artists\": [\n");
        for (int i = 0; i < artists.size(); i++) {
            String name = artists.get(i);
            json.append("\"" + name + "\"");

            if (i != artists.size() - 1)
                json.append(",");
        }

        json.append(" ]}");

        return json.toString();
    }

    private List<String> favorites;

    private String provideFavoritesJsonArray() {
        StringBuilder json = new StringBuilder();
        json.append("{\n" + "  \"songIds\": [\n");
        for (int i = 0; i < favorites.size(); i++) {
            String name = favorites.get(i);
            json.append("\"" + name + "\"");

            if (i != favorites.size() - 1)
                json.append(",");
        }

        json.append(" ]}");

        return json.toString();
    }

}
