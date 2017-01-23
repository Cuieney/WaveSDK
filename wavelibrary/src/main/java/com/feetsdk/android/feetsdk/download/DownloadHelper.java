package com.feetsdk.android.feetsdk.download;

import android.content.Context;

import com.feetsdk.android.FeetConfig;
import com.feetsdk.android.common.utils.LocalPathResolver;
import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.db.domain.table.FavSong;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;
import com.feetsdk.android.feetsdk.entity.DownloadProgress;
import com.feetsdk.android.feetsdk.entity.UpdateProgress;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cuieney on 16/9/9.
 */
public class DownloadHelper {

    //下载回调接口
    private final FileDownloadListener downloadListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            updateSongDownloadProgress(new ProgressBus(task, soFarBytes, totalBytes));
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            updateSongDownloadProgress(new ProgressBus(task, 1, 1));
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Logger.e("paused");
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            Logger.e("error"+e.getMessage().toString());

        }

        @Override
        protected void warn(BaseDownloadTask task) {
            Logger.e("warn");
        }
    };

    //下载队列任务
    private FileDownloadQueueSet queueSet;
    //数据库助手
    private OrmHelper ormHelper;
    //下载任务
    private final List<BaseDownloadTask> tasks;

    private final Context context;


    private IUpdateProgressCallBack callBack;
    private FeetConfig config;

    private int type;

    /**
     * 初始化下载器
     */
    public DownloadHelper(Context context, IUpdateProgressCallBack callback) {
        config = new FeetConfig(context);
        File file = new File(LocalPathResolver.getDir());
        File img = new File( LocalPathResolver.getImgDir());
        if (!file.exists()) {
            file.mkdirs();
        }

        if (!img.exists()) {
            img.mkdirs();
        }

        this.context = context;
        this.callBack = callback;
        ormHelper = new OrmHelper(context);
//        FileDownloader.setGlobalPost2UIInterval(500);
//        FileDownloader.setGlobalHandleSubPackageSize(20);

        queueSet = new FileDownloadQueueSet(downloadListener);
        tasks = new ArrayList<>();

    }


    /**
     * 开始下载
     */
    public void startDownload(int type) {
        this.type = type;
        addDownloadTask();
        //queueSet.disableCallbackProgressTimes(); //每首歌都返回进度

        // 所有任务在下载失败的时候都自动重试一次
        queueSet.setAutoRetryTimes(3);

        // 串行执行该任务队列
        queueSet.downloadSequentially(tasks);
        //并行执行该任务队列
//        queueSet.downloadTogether(tasks);
        queueSet.start();
    }


    /**
     * 暂停所有任务
     */
    public void pauseDownload() {
        FileDownloader.getImpl().pauseAll();
        if (callBack != null) {
            callBack = null;
        }
    }

    public void pauseReceiver() {
        FileDownloader.getImpl().pauseAll();
    }

    /**
     * 添加下载任务
     */
    public void addDownloadTask() {


        if (type == EventType.DOWNLOAD_GET_MUSIC) {
            List<LocalSongs> songList = ormHelper.queryAllSong();
            if (songList.size() > 0) {
                for (LocalSongs msc : songList) {
                    if (msc.getProgress() != 100) {
                        String file = msc.getSongId() + ".mp3";
                        String image = msc.getSongId() + ".jpg";
                        BaseDownloadTask object = FileDownloader.getImpl().create(msc.getMp3Url()).setTag(file);
                        object.setPath(LocalPathResolver.getDir() + file);

                        BaseDownloadTask img = FileDownloader.getImpl().create(msc.getCoverImageUrl()).setTag(image);
                        img.setPath( LocalPathResolver.getImgDir() + image);

                        tasks.add(img);
                        tasks.add(object);
                    }
                }
            }
        } else {
            List<FavSong> favSongList = ormHelper.queryAllFavMsc();
            if (favSongList.size() > 0) {
                for (FavSong msc : favSongList) {
                    if (msc.getProgress() != 100) {
                        String file = msc.getSongId() + ".mp3";
                        String image = msc.getSongId() + ".jpg";
                        BaseDownloadTask object = FileDownloader.getImpl().create(msc.getMp3Url()).setTag(file);
                        object.setPath(LocalPathResolver.getDir() + file);

                        BaseDownloadTask img = FileDownloader.getImpl().create(msc.getCoverImageUrl()).setTag(image);
                        img.setPath( LocalPathResolver.getImgDir() + image);

                        tasks.add(img);
                        tasks.add(object);
                    }
                }
            }
        }

    }


    /**
     * 更新歌曲下载进度
     *
     * @param progressBus bus进度数据
     */
    private void updateSongDownloadProgress(ProgressBus progressBus) {
        float current = progressBus.getSoFarBytes();
        float total = progressBus.getTotalByte();
        BaseDownloadTask task = progressBus.getTask();
        String[] split = ((String) task.getTag()).split("\\.");
        float progress = (current / total) * 100;
        if (type == EventType.DOWNLOAD_GET_MUSIC) {
            if (split[1].equals("jpg")) {
                ormHelper.updateImgDownloadProgress(split[0], ((int) progress));
            }else if(split[1].equals("mp3")){
                ormHelper.updateSongDownloadProgress(split[0], ((int) progress));
            }

        } else {
            if (split[1].equals("jpg")) {
                ormHelper.updateImgFavMscProgress(split[0], ((int) progress));
            }else if(split[1].equals("mp3")){
                ormHelper.updateFavMscProgress(split[0], ((int) progress));
            }


        }
        updateProgress();
    }

    /**
     * 发送全局进度
     */
    private void updateProgress() {

        if (type == EventType.DOWNLOAD_GET_MUSIC) {
            List<LocalSongs> localSongses = ormHelper.queryAllSong();
            int totalMinute = 0;
            int currentProgress = 0;
            float totalProgress = config.getDefaultMusciSize() * 100;
            for (LocalSongs msc : localSongses) {
                if (!msc.getListener()) {
                    currentProgress += msc.getProgress();
                    if (msc.getProgress() == 100 && msc.getProgress() == 100) {
                        totalMinute += msc.getSize();
                    }
                }
            }
            float percent = (currentProgress / totalProgress) * 100;
            if (callBack != null) {
                callBack.progress(new UpdateProgress(((int) percent), totalMinute / 60));
            }

        } else if (type == EventType.DOWNLOAD_GET_FAVORITE_MUSIC) {
            List<FavSong> favSongs = ormHelper.queryAllFavMsc();
            int totalMinute = 0;
            int currentProgress = 0;
            float totalProgress = favSongs.size() * 100;
            for (FavSong msc : favSongs) {
                currentProgress += msc.getProgress();
                if (msc.getProgress() == 100 && msc.getProgress() == 100) {
                    totalMinute += msc.getSize();
                }
            }
            float percent = (currentProgress / totalProgress) * 100;
            if (callBack != null) {
                callBack.progress(new UpdateProgress(((int) percent), totalMinute / 60));
            }


        }

    }


    /**
     * 获取全局下载进度
     *
     * @return
     */
    public DownloadProgress getGlobalProgress() {

        List<LocalSongs> localSongses = ormHelper.queryAllSong();
        int totalMinute = 0;
        int currentProgress = 0;
        float totalProgress = config.getDefaultMusciSize() * 100;
        for (LocalSongs msc : localSongses) {
//            if (!msc.getListener()) {
            if (true) {
                currentProgress += msc.getProgress();
                if (msc.getProgress() == 100 && msc.getProgress() == 100) {
                    totalMinute += msc.getSize();
                }
            }
        }
        float percent = (currentProgress / totalProgress) * 100;

        List<FavSong> favSongs = ormHelper.queryAllFavMsc();
        int totalFavMinute = 0;
        int currentFavProgress = 0;
        float totalFavProgress = favSongs.size() * 100;
        for (FavSong msc : favSongs) {
            currentFavProgress += msc.getProgress();
            if (msc.getProgress() == 100 && msc.getProgress() == 100) {
                totalFavMinute += msc.getSize();
            }
        }
        float percentFav = (currentFavProgress / totalFavProgress) * 100;

        return new DownloadProgress(((int) percent),totalMinute/60, ((int) percentFav),totalFavMinute/60);
    }


}
