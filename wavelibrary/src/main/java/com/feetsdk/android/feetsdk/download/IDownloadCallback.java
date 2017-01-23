package com.feetsdk.android.feetsdk.download;

import com.feetsdk.android.feetsdk.annotation.RequestStartDownload;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;
import com.feetsdk.android.feetsdk.entity.DownloadProgress;

import java.util.List;

/**
 * Created by cuieney on 16/11/14.
 */


public interface IDownloadCallback {
    int GET_MUSIC = 1;
    int GET_FAVORITE_MUSIC = 2;
    void startDownload(@RequestStartDownload int type);
    void pauseDownload();
    DownloadProgress getProgress();
    void restart(@RequestStartDownload int type);
    void resetPause();
    List<LocalSongs> getMusic();

}
