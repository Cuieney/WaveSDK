package com.feetsdk.android.feetsdk.music;

import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;

import java.util.List;

/**
 * Created by cuieney on 16/11/22.
 */
public interface IMusicCallBack {

    List<Music> getListenedMusic();
    List<Music> getFavoriteMusic();

    void favFromSong(String id ,IHttpRspCallBack callBack);
    void deleteFromSong(String id ,IHttpRspCallBack callBack);
    void favFromMsc(String id ,IHttpRspCallBack callBack);
    void deleteFromMsc(String id ,IHttpRspCallBack callBack);
    void clearFeetSdk();

}
