package com.feetsdk.android.feetsdk.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.stepcount.IStepChange;

/**
 * Created by cuieney on 17/1/4.
 */
public class MusicController implements PlaybackManager.MyMediaSession {
    public final MusicProxy musicProxy;
    private Context mContext;
    private MediaControllerCompat mController;
    //    private WeakReference<OnMusicChangeListener> wrOnMusicChange;
//    private WeakReference<OnMediaControllerListener> wrOnMediaController;
    private OnMusicChangeListener onMusicChangeListener;
    private OnMediaControllerListener onMediaControllerListener;
    private OnMediaStateUpdatedListener onMediaStateUpdatedListener;
    private IStepChange stepChange;
    private final BroadcastReceiver statueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            switch (intent.getAction()) {
                case MusicProxy.ACTION_MEDIA_DATA:
                    synchronized (MusicController.class) {
                        Music mMetadataCompat = extras.getParcelable(MusicProxy.KEY_MEDIA_DATA);
                        if (onMusicChangeListener != null) {
                            onMusicChangeListener.onMusicChange(mMetadataCompat);
                        }
                    }
                    break;
                case MusicProxy.ACTION_MEDIA_SESSION:
                    MediaSessionCompat.Token token = extras.getParcelable(MusicProxy.KEY_TOKEN);
                    try {
                        if (mController == null) {
                            mController = new MediaControllerCompat(context, token);
                            if (onMediaControllerListener != null) {
                                onMediaControllerListener.onMediaControllerChange();
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Logger.e(e.getMessage());
                    }

                    break;
                case MusicProxy.STEP_CHANGE_BPM:
                    int bpm = intent.getIntExtra("bpm", 120);
                    if (stepChange != null) {
                        stepChange.getCurrentBpm(bpm);
                    }
                    break;
                case MusicProxy.STEP_CHANGE_STEP:
                    double stepCount = intent.getDoubleExtra("stepCount", 0.0);
                    if (stepChange != null) {
                        stepChange.getStepCount(stepCount);
                    }
                    break;
                case MusicProxy.ACTION_PLAYBACK_STATE:
                    synchronized (MusicController.class) {
                        Bundle extras1 = intent.getExtras();
                        PlaybackStateCompat stateCompat = extras1.getParcelable(MusicProxy.KEY_PLAYBACK_STATE);
                        if (onMediaStateUpdatedListener != null) {
                            onMediaStateUpdatedListener.OnMediaStateUpdated(stateCompat);
                        }
                    }
                    break;
            }
        }
    };

    public MusicController(Context context) {
        this.mContext = context.getApplicationContext();
        musicProxy = new MusicProxy(context);
        init();
    }


    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicProxy.ACTION_MEDIA_DATA);
        filter.addAction(MusicProxy.ACTION_MEDIA_SESSION);
        filter.addAction(MusicProxy.STEP_CHANGE_BPM);
        filter.addAction(MusicProxy.STEP_CHANGE_STEP);
        filter.addAction(MusicProxy.ACTION_PLAYBACK_STATE);
        mContext.getApplicationContext().registerReceiver(statueReceiver, filter);
    }

    @Override
    public void onPlay() {
        musicProxy.init();
    }

    @Override
    public void onSeekTo(long percent) {
        musicProxy.setSeek(((int) percent));
    }

    @Override
    public void onPause() {
        musicProxy.pauseMusic();
    }

    public void onContinue() {
        musicProxy.continueMusic();
    }

    @Override
    public void onStop() {
        mContext.getApplicationContext().unregisterReceiver(statueReceiver);
        unregisterMusicChangeListener();
        unregisterMediaControllerListener();
        unregisterStepChange();
        unregisterMusicUpdatedListener();
        musicProxy.stopMusic();
    }

    @Override
    public void onSkipToNext() {
        musicProxy.nextMusic();
    }

    @Override
    public void onSkipToPrevious() {
        musicProxy.prevMusic();
    }

    @Override
    public void setTempo(float tempo) {
        musicProxy.setTempo(tempo);
    }

    @Override
    public void onCustomAction(String action, Bundle extras) {
        musicProxy.customeAction(action, extras);
    }

    public void registerMusicChangeListener(OnMusicChangeListener onMusicChangeListener) {
//        wrOnMusicChange = new WeakReference<>(onMusicChangeListener);
        this.onMusicChangeListener = onMusicChangeListener;
    }

    public void registerMediaControllerChangeListener(OnMediaControllerListener onMediaControllerListener) {
//        wrOnMediaController = new WeakReference<>(onMediaControllerListener);
        this.onMediaControllerListener = onMediaControllerListener;
    }

    public void registerStepChange(IStepChange stepChange) {
        this.stepChange = stepChange;
    }

    public void registerMeidaUpdatedListener(OnMediaStateUpdatedListener onMediaStateUpdatedListener) {
        this.onMediaStateUpdatedListener = onMediaStateUpdatedListener;
    }

    public void unregisterMusicChangeListener() {
        onMusicChangeListener = null;
    }

    public void unregisterMusicUpdatedListener() {
        onMediaStateUpdatedListener = null;
    }

    public void unregisterMediaControllerListener() {
        onMediaControllerListener = null;
    }

    public void unregisterStepChange() {
        stepChange = null;
    }

    public MediaControllerCompat getMediaControls() {
        return mController;
    }
}
