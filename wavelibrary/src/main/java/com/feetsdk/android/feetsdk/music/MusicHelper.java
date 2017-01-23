package com.feetsdk.android.feetsdk.music;

import android.content.Context;

import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by cuieney on 16/11/22.
 */
public class MusicHelper implements IMusicCallBack {

    private WeakReference<Context> weakReference;
    private MusicProxy proxy;
    public MusicHelper(WeakReference<Context> weakReference) {
        this.weakReference = weakReference;
        proxy = new MusicProxy(weakReference);
    }

    @Override
    public List<Music> getListenedMusic() {
        return proxy.getListenedMusic();
    }

    @Override
    public List<Music> getFavoriteMusic() {
        return proxy.getFavoriteMusic();
    }

    @Override
    public void favFromSong(String id, IHttpRspCallBack callBack) {
        WeakReference<IHttpRspCallBack> reference = new WeakReference<IHttpRspCallBack>(callBack);
        proxy.favFromSong(id,reference.get());
    }

    @Override
    public void deleteFromSong(String id, IHttpRspCallBack callBack) {
        WeakReference<IHttpRspCallBack> reference = new WeakReference<IHttpRspCallBack>(callBack);
        proxy.deleteFromSong(id,reference.get());
    }

    @Override
    public void favFromMsc(String id, IHttpRspCallBack callBack) {
        WeakReference<IHttpRspCallBack> reference = new WeakReference<IHttpRspCallBack>(callBack);
        proxy.favFromMsc(id,reference.get());
    }

    @Override
    public void deleteFromMsc(String id, IHttpRspCallBack callBack) {
        WeakReference<IHttpRspCallBack> reference = new WeakReference<IHttpRspCallBack>(callBack);
        proxy.deleteFromMsc(id,reference.get());
    }

    @Override
    public void clearFeetSdk() {
        proxy.clearFeetSdk();
    }

}
