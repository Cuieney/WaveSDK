package com.feetsdk.android.feetsdk.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cuieney on 16/11/15.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({EventType.DOWNLOAD_GET_MUSIC,EventType.DOWNLOAD_GET_FAVORITE_MUSIC})
public @interface RequestStartDownload {
}
