package com.feetsdk.android.feetsdk.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cuieney on 16/11/22.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({EventType.PLAY_MUSIC_TYPE_COLLECTION,EventType.PLAY_MUSIC_TYPE_NORMAL})
public @interface PlayMusicType {
}
