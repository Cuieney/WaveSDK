package com.feetsdk.android.feetsdk.musicplayer;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by cuieney on 17/1/17.
 */
public interface OnMediaStateUpdatedListener {
    void OnMediaStateUpdated(PlaybackStateCompat playbackStateCompat);
}
