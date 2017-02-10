package com.feetsdk.android.feetsdk.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;


/**
 * Created by cuieney on 17/1/3.
 */
public class LocalPlayback implements Playback, FeetPlayer.OnPreparedListener, FeetPlayer.OnCompletionCallBack, FeetPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    public static final int VOLUME_DUCK = 10;
    public static final int VOLUME_NORMAL = 100;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;

    public WifiManager.WifiLock mWifiLock;
    public AudioManager mAudioManager;
    private Context mContext;
    private int mState;
    private Callback mCallback;
    private volatile int mCurrentPosition;
    private volatile String mCurrentMediaId;
    private FeetPlayer mMediaPlayer;
    private boolean mPlayOnFocusGain;

    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private volatile boolean mAudioNoisyReceiverRegistered;

    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    Intent i = new Intent(context, MusicService.class);
                    i.setAction(MusicService.ACTION_CMD);
                    i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE);
                    mContext.startService(i);
                }
            }
        }
    };

    private float mTempo;

    public LocalPlayback(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        this.mState = PlaybackStateCompat.STATE_NONE;
    }


    @Override
    public void start() {
    }

    @Override
    public void stop(boolean notifyListeners) {
        mState = PlaybackStateCompat.STATE_STOPPED;
        if (notifyListeners && mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        mCurrentPosition = getCurrentStreamPosition();
        // Give up Audio focus
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        // Relax all resources
        relaxResources(true);
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public int getCurrentStreamPosition() {
        return mMediaPlayer != null ?
                ((int) mMediaPlayer.getPositonSeconds()) : mCurrentPosition;
    }

    @Override
    public void play(String path) {
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();

        boolean mediaHasChanged = !TextUtils.equals(path, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentMediaId = path;
        }

        if (mState == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else {
            mState = PlaybackStateCompat.STATE_STOPPED;
            relaxResources(false); // release everything except MediaPlayer

            createMediaPlayerIfNeeded();

            mState = PlaybackStateCompat.STATE_PLAYING;

            mMediaPlayer.play(mCurrentMediaId, true);
            // If we are streaming from the internet, we want to hold a
            // Wifi lock, which prevents the Wifi radio from going to
            // sleep while the song is playing.
            mWifiLock.acquire();

            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }

        }

    }

    @Override
    public void pause() {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.onPlayPause(false);
                mCurrentPosition = ((int) mMediaPlayer.getPositonSeconds());
            }
            // while paused, retain the MediaPlayer but give up audio focus
            relaxResources(false);
            giveUpAudioFocus();
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        unregisterAudioNoisyReceiver();
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer == null) {
            // If we do not have a current media player, simply update the current position
            mCurrentPosition = position;
        } else {
            if (mMediaPlayer.isPlaying()) {
                mState = PlaybackStateCompat.STATE_BUFFERING;
            }
            long durationSeconds = mMediaPlayer.getDurationSeconds();
            float percent = ((float) (position / durationSeconds));
            mMediaPlayer.setSeek(((int) (percent * 100)));
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }

    @Override
    public void setTempo(float tempo) {
        this.mTempo = tempo;
        mMediaPlayer.setTempo(tempo);
    }

    @Override
    public float getTempo() {
        return mTempo;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new FeetPlayer(mContext);

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnComplicationListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    @Override
    public void onPrepared(FeetPlayer fp) {
        mState = PlaybackStateCompat.STATE_PLAYING;
        configMediaPlayerState();
    }

    @Override
    public void onCompletionListener(FeetPlayer fp) {
        if (mCallback != null) {
            mCallback.onCompletion();
        }
    }

    @Override
    public void onErrorListener(FeetPlayer fp, int what, int extra) {
        if (mCallback != null) {
            mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
        }
    }


    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private void configMediaPlayerState() {
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                pause();
            }
        } else {  // we have audio focus:
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK); // we'll be relatively quiet
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL); // we can be loud again
                } // else do something for remote client.
            }
            // If we were playing when we lost focus, we need to resume playing.
                if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    long positonSeconds = mMediaPlayer.getPositonSeconds();
                    if (((int) positonSeconds) - mCurrentPosition <= 3) {
                        mMediaPlayer.onPlayPause(true);
                        mState = PlaybackStateCompat.STATE_PLAYING;
                    } else {
                        long durationSeconds = mMediaPlayer.getDurationSeconds();
                        float percent = (((float) mCurrentPosition) / ((float) durationSeconds));
                        mMediaPlayer.setSeek(((int) (percent * 100)));
                        mState = PlaybackStateCompat.STATE_BUFFERING;
                        mMediaPlayer.onPlayPause(true);
                        mState = PlaybackStateCompat.STATE_PLAYING;
                    }
                }
                mPlayOnFocusGain = false;
            }
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    /**
     * Try to get the system audio focus.
     */
    private void tryToGetAudioFocus() {
        if (mAudioFocus != AUDIO_FOCUSED) {
            int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_FOCUSED;
            }
        }
    }

    /**
     * Give up the audio focus.
     */
    private void giveUpAudioFocus() {
        if (mAudioFocus == AUDIO_FOCUSED) {
            if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mAudioFocus = AUDIO_FOCUSED;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if (mState == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                mPlayOnFocusGain = true;
            }
        } else {
        }
        configMediaPlayerState();
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *                           be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }
}
