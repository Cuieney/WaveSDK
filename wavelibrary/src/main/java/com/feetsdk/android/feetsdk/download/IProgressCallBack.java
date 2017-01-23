package com.feetsdk.android.feetsdk.download;

import com.feetsdk.android.feetsdk.entity.DownloadProgress;

/**
 * Created by cuieney on 16/11/14.
 */
public interface IProgressCallBack {

    void progress(DownloadProgress progress);
}
