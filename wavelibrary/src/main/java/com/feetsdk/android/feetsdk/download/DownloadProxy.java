package com.feetsdk.android.feetsdk.download;

import android.content.Context;

import com.feetsdk.android.FeetConfig;
import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.common.utils.LocalPathResolver;
import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.common.utils.NetWorkUtil;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.annotation.RequestStartDownload;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.db.domain.table.FavSong;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;
import com.feetsdk.android.feetsdk.entity.DownloadProgress;
import com.feetsdk.android.feetsdk.entity.response.RspMusic;
import com.feetsdk.android.feetsdk.http.HttpControler;
import com.feetsdk.android.feetsdk.http.HttpResponse;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/11/30.
 */
public class DownloadProxy implements IDownloadCallback {
    private WeakReference<Context> contextWeakReference;
    private DownloadHelper downloadHelper;
    private OrmHelper ormHelper;
    private FeetConfig config;
    private HttpControler controler;
    private WeakReference<IUpdateProgressCallBack> callbackWeakReference;


    public DownloadProxy(Context context, IUpdateProgressCallBack callback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.callbackWeakReference = new WeakReference<>(callback);

        downloadHelper = new DownloadHelper(context, callback);
        ormHelper = new OrmHelper(context);

        config = new FeetConfig(context);
        controler = new HttpControler(context);

    }

    public DownloadProxy(Context context) {
        this.contextWeakReference = new WeakReference<>(context);

        downloadHelper = new DownloadHelper(context, null);
        ormHelper = new OrmHelper(context);

        config = new FeetConfig(context);
        controler = new HttpControler(context);


    }

    private IUpdateProgressCallBack getCallback() {
        return callbackWeakReference.get();
    }

    private Context getContext() {
        return contextWeakReference.get();
    }

    public void setCallback(IUpdateProgressCallBack callback) {
        downloadHelper = null;
        this.callbackWeakReference = new WeakReference<>(callback);
        downloadHelper = new DownloadHelper(getContext(), callback);
    }

    private int type;


    @Override
    public void startDownload(@RequestStartDownload int type) {
        this.type = type;
        if (NetWorkUtil.isNetworkAvailable(getContext()) && !config.getAppkey().equals("")) {
            NetWorkUtil.NetType netType = NetWorkUtil.getNetworkType(getContext());

            if (netType.equals(NetWorkUtil.NetType.NETWORK_WIFI)) {
                //wifi下进行下载
                downloadMusic();
            } else {
                if (config.getCellularData()) {
                    //  判断用户是否允许流量情况下载
                    downloadMusic();
                }
            }

        }
    }

    private void downloadMusic() {
        boolean isFirst = false;
        boolean isComplete = false;
        int deleteNum = 0;
        int totalNum = 0;


        List<FavSong> favSongs = ormHelper.queryAllFavMsc();
        List<LocalSongs> localSongses = ormHelper.queryAllSong();
        if (localSongses.size() <= 0) {
            isFirst = true;
        }


        //删除收藏表中操作后的垃圾数据
        for (FavSong msc : favSongs) {
            if (!msc.getCollection()) {
                ormHelper.deleteFavMsc(msc.getSongId());
            }
        }



        if (isFirst) {
            getMusic(config.getDefaultMusciSize());
        } else {

            //不是第一次 收藏进来不操作
            if (type == EventType.DOWNLOAD_GET_FAVORITE_MUSIC) {
                if (favSongs.size() <= 0) {
                    getMusic(config.getDefaultMusciSize());
                }else{
                    //判断是否有没有下载完的收藏歌曲
                    for (LocalSongs song : localSongses) {
                        if (song.getProgress() != 100) {
                            downloadHelper.startDownload(type);
                            break;
                        }
                    }
                }

                return;
            }

            //删除听过没有收藏的歌曲
            for (LocalSongs msc : localSongses) {
                if (msc.getListener()) {
                    deleteNum++;
                    ormHelper.deleteSong(msc.getSongId());
                }else {
                    totalNum++;
                }
            }

            deleteNum = config.getDefaultMusciSize() - totalNum;
//            if (deleteNum == 0) {
//            }



            //是否全部下载完成
            for (LocalSongs song : localSongses) {
                if (song.getProgress() != 100) {
                    isComplete = false;
                    break;
                } else {
                    isComplete = true;
                }
            }

            //一首没听过
            if (deleteNum == 0) {
                //情况1.全部下载好了，情况2.下载一部分，
                if (isComplete) {
                    return;
                } else {
                    downloadHelper.startDownload(type);
                }
                //听过一首歌以上
            } else {
                getMusic(deleteNum);
            }
        }

    }


    //拉取新歌曲进行下载
    private void getMusic(int deleteIndex) {
        Logger.d("download music number"+deleteIndex);
        if (type == EventType.DOWNLOAD_GET_MUSIC) {
            controler.getMusic(deleteIndex, new IHttpRspCallBack() {
                @Override
                public void success(HttpResponse response) {
                    if (response.getCode() == 200){
                        Logger.d(response.getMessage());
                        download(response.getMessage());
                    }
                }

                @Override
                public void failed(HttpException exception) {
                    throw new IllegalArgumentException("http connected failed");
                }
            });

        } else if (type == EventType.DOWNLOAD_GET_FAVORITE_MUSIC) {
            controler.getFavorites(new IHttpRspCallBack() {
                @Override
                public void success(HttpResponse response) {
                    if (response.getCode() == 200){
                        Logger.d(response.getMessage());
                        download(response.getMessage());
                    }
                }

                @Override
                public void failed(HttpException exception) {
                    throw new IllegalArgumentException("http connected failed");
                }
            });
        }


    }


    private void download(String music) {
        List<RspMusic> musics = new ArrayList<>();
//        List<RspMusic> musics = JSON.parseArray(music, RspMusic.class);
//        for (RspMusic msc : musics) {
//            Logger.d(msc.toString());
//        }
        try {
            JSONArray jsonArray = new JSONArray(music);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                RspMusic rspMusic = new RspMusic();
                rspMusic.setName(jsonObject.getString("name"));
                rspMusic.setArtistName(jsonObject.getString("artistName"));
                rspMusic.setTempo(jsonObject.getDouble("tempo"));
                rspMusic.setDuration(jsonObject.getDouble("duration"));
                rspMusic.setSongId(jsonObject.getString("songId"));
                rspMusic.setMp3Url(jsonObject.getString("mp3Url"));
                rspMusic.setCoverImageUrl(jsonObject.getString("coverImageUrl"));
                rspMusic.setArtistId(jsonObject.getString("artistId"));
                rspMusic.setFavorited(jsonObject.optBoolean("isFavorited"));
                musics.add(rspMusic);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (musics.size() > 0) {
            for (RspMusic msc : musics) {
                int progress = 0;
                int imgProgress = 0;
                String id = msc.getSongId();

                //查询本地是否有下载好的歌曲
                LocalSongs localSong = ormHelper.querySong(msc.getSongId());
                if (localSong != null) {
                    progress = localSong.getProgress();
                    imgProgress = localSong.getImgProgress();
                }

                if (type == EventType.DOWNLOAD_GET_MUSIC) {
                    id = resetName(id);
                    //拉下来的有下载的 在下载的数据库中也添加一份
                    ormHelper.insertSongToLocal(new LocalSongs(
                            id,
                            msc.getName(),
                            msc.getCoverImageUrl(),
                            progress,
                            msc.getMp3Url(),
                            LocalPathResolver.getDir() + msc.getSongId()+".mp3",
                            msc.getArtistName(),
                            ((int) msc.getTempo()) + "",
                            (int) msc.getDuration(),
                            msc.isFavorited(), false, imgProgress,
                            LocalPathResolver.getImgDir()+ msc.getSongId()+".jpg"
                            ));

                } else if (type == EventType.DOWNLOAD_GET_FAVORITE_MUSIC) {

                    ormHelper.insertFavMsc(new FavSong(
                            msc.getSongId(),
                            msc.getName(),
                            msc.getCoverImageUrl(),
                            progress,
                            msc.getMp3Url(),
                            LocalPathResolver.getDir() + msc.getSongId()+".mp3",
                            msc.getArtistName(),
                            ((int) msc.getTempo()) + "",
                            ((int) msc.getDuration()),
                            true,imgProgress,
                            LocalPathResolver.getImgDir() + msc.getSongId()+".jpg"
                    ));
                }


            }
        }

        //开始下载
        downloadHelper.startDownload(type);
    }

    //重新命名
    private String resetName(String id) {
        String newId = id;
        List<LocalSongs> songsListDb = ormHelper.queryAllSong();
        if (songsListDb.size() > 0) {
            for (int i = 0; i < songsListDb.size(); i++) {
                LocalSongs songs = songsListDb.get(i);
                if (newId.equals(songs.getSongId())) {
                    resetName(newId + "_" + i);
                }
            }
        }

        return newId;

    }

    @Override
    public void pauseDownload() {
        if (downloadHelper != null) {
            downloadHelper.pauseDownload();
        }
        if (ormHelper != null) {
            ormHelper.closeDb();
            callbackWeakReference = null;
            controler = null;
            downloadHelper = null;
        }
    }

    @Override
    public DownloadProgress getProgress() {
        return downloadHelper.getGlobalProgress();
    }

    @Override
    public void restart(@RequestStartDownload int type) {
        List<LocalSongs> localSongses = ormHelper.queryAllSong();
        this.type = type;
        synchronized (DownloadProxy.class) {
            if (localSongses.size() <= 0) {
                if (controler != null) {
                    getMusic(config.getDefaultMusciSize());
                }
            } else {
                startDownload(type);
            }
        }


    }

    @Override
    public void resetPause() {
        downloadHelper.pauseReceiver();
    }

    @Override
    public List<LocalSongs> getMusic() {
        return ormHelper.getMusic();
    }


    public void delete() {
        ormHelper.deleteAllSong();
    }
}
