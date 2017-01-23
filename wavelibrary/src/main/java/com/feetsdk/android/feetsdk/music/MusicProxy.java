package com.feetsdk.android.feetsdk.music;

import android.content.Context;

import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.db.domain.table.FavSong;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;
import com.feetsdk.android.feetsdk.http.HttpControler;
import com.feetsdk.android.feetsdk.http.HttpResponse;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/11/22.
 */
public class MusicProxy implements IMusicCallBack {

    private OrmHelper ormHelper;
    private HttpControler httpControler;
    private WeakReference<Context> contextWeakReference;

    public MusicProxy(WeakReference<Context> weakReference) {
        this.contextWeakReference = weakReference;
        ormHelper = new OrmHelper(weakReference.get());
        httpControler = new HttpControler(weakReference.get());
    }

    @Override
    public List<Music> getListenedMusic() {
        List<Music> musicList = new ArrayList<>();
        List<LocalSongs> localSongsList = ormHelper.queryAllSong();
        if (localSongsList.size() > 0) {
            for (LocalSongs song : localSongsList) {
                if (song.getListener()) {
                    musicList.add(song2Music(song));
                }
            }
        }
        return musicList;
    }

    @Override
    public List<Music> getFavoriteMusic() {
        List<Music> musicList = new ArrayList<>();
        List<FavSong> localSongsList = ormHelper.queryAllFavMsc();
        if (localSongsList.size() > 0) {
            for (FavSong song : localSongsList) {
                if (song.getCollection()) {
                    musicList.add(favMsc2Music(song));
                }
            }
        }
        return musicList;
    }

    //第一张表操作第二张表
    @Override
    public void favFromSong(final String id, final IHttpRspCallBack callBack) {
        LocalSongs localSongs = ormHelper.querySong(id);
        if (localSongs != null) {
            if (!localSongs.getCollection()) {
                httpControler.postFavorites(new IHttpRspCallBack() {
                    @Override
                    public void success(HttpResponse response) {
                        if (response.getCode() == 200) {
                            ormHelper.updateFavSong(id);
                            callBack.success(response);
                        }
                    }

                    @Override
                    public void failed(HttpException exception) {
                        callBack.failed(exception);
                    }
                }, localSongs.getSongId());
            }
        }
    }

    //第一张表操作第二张表
    @Override
    public void deleteFromSong(final String id, final IHttpRspCallBack callBack) {
        LocalSongs localSongs = ormHelper.querySong(id);
        if (localSongs != null) {
            if (localSongs.getCollection()) {
                httpControler.deleteFavorite(localSongs.getSongId(), new IHttpRspCallBack() {
                    @Override
                    public void success(HttpResponse response) {
                        if (response.getCode() == 200) {
                            ormHelper.updateFavSong(id);
                            callBack.success(response);
                        }
                    }

                    @Override
                    public void failed(HttpException exception) {
                        callBack.failed(exception);
                    }
                });
            }
        }
    }

    //第二张表操作第一张表
    @Override
    public void favFromMsc(final String id, final IHttpRspCallBack callBack) {
        final FavSong favSong = ormHelper.queryFavMsc(id);
        if (favSong != null) {
            if (!favSong.getCollection()) {
                httpControler.postFavorites(new IHttpRspCallBack() {
                    @Override
                    public void success(HttpResponse response) {
                        if (response.getCode() == 200) {
                            ormHelper.updateFavCollection(id);
                            callBack.success(response);
                        }
                    }

                    @Override
                    public void failed(HttpException exception) {
                        callBack.failed(exception);
                    }
                }, favSong.getSongId());
            }
        }
    }

    //第二张表操作第一张表
    @Override
    public void deleteFromMsc(final String id, final IHttpRspCallBack callBack) {
        final FavSong favSong = ormHelper.queryFavMsc(id);
        if (favSong != null) {
            if (favSong.getCollection()) {
                httpControler.deleteFavorite(favSong.getSongId(), new IHttpRspCallBack() {
                    @Override
                    public void success(HttpResponse response) {
                        if (response.getCode() == 200) {
                            ormHelper.updateFavCollection(id);
                            callBack.success(response);
                        }
                    }

                    @Override
                    public void failed(HttpException exception) {
                        callBack.failed(exception);
                    }
                });
            }
        }
    }

    @Override
    public void clearFeetSdk(){
        ormHelper.clearData();
    }


    private Music song2Music(LocalSongs song) {
        Music msc = new Music(song.getSongId(), song.getSongName(), song.getCoverImageUrl(),
                song.getProgress(), song.getPath(), song.getSingerName(), song.getTempo(),
                song.getSize(), song.getCollection(), song.getListener(),song.getImgPath());
        return msc;
    }

    private Music favMsc2Music(FavSong song) {
        Music msc = new Music(
                song.getSongId(),
                song.getSongName(),
                song.getCoverImageUrl(),
                song.getProgress(),
                song.getPath(),
                song.getSingerName(),
                song.getTempo(),
                song.getSize(), true, true,
                song.getImgPath()
        );
        return msc;
    }
}
