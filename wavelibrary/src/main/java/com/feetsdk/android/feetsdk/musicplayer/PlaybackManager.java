package com.feetsdk.android.feetsdk.musicplayer;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;

/**
 * Created by cuieney on 17/1/3.
 */
public class PlaybackManager implements Playback.Callback {

    private QueueManager mQueueManager;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;


    public PlaybackManager(PlaybackServiceCallback mServiceCallback, QueueManager mQueueManager,
                           Playback mPlayback) {
        this.mQueueManager = mQueueManager;
        this.mPlayback = mPlayback;
        this.mServiceCallback = mServiceCallback;
        mMediaSessionCallback = new MediaSessionCallback();
        this.mPlayback = mPlayback;
        mPlayback.setCallback(this);
    }

    @Override
    public void onCompletion() {
        if (mQueueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }



    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        LocalSongs currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic.getPath());
        }
    }

    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }


    public void handleSetSeekRequest(long position){
        mPlayback.seekTo(((int) position));
    }

    public void handleSetTempoRequest(float tempo){
        float rawTempo = Float.parseFloat(mQueueManager.getCurrentMusic().getTempo());
        mPlayback.setTempo( tempo / rawTempo);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null ) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }



    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private class MediaSessionCallback implements MyMediaSession {


        @Override
        public void onPlay() {
            if (mQueueManager.getCurrentMusic() == null) {
                return;
            }
            handlePlayRequest();
            mQueueManager.updateMetadata();
        }


        @Override
        public void onSeekTo(long position) {
            handleSetSeekRequest(position);
        }


        @Override
        public void onPause() {
            handlePauseRequest();
        }


        @Override
        public void onStop() {
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            Logger.e("onSkipToNext");
            if (mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            Logger.e("onSkipToPrevious");
            if (mQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void setTempo(float tempo) {
            handleSetTempoRequest(tempo);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            mQueueManager.song2Item();
            mQueueManager.setCurrentQueueIndex(0);
        }
    }

    public MyMediaSession getMediaSessionCallback() {
        return mMediaSessionCallback;
    }
    public Playback getPlayback() {
        return mPlayback;
    }
    public interface PlaybackServiceCallback {
        void onPlaybackStart();
        void onPlaybackStop();
        void onNotificationRequired();
        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }


    public interface MyMediaSession{
        void onPlay();
        void onSeekTo(long position);
        void onPause();
        void onStop();
        void onSkipToNext();
        void onSkipToPrevious();
        void setTempo(float tempo);
        void onCustomAction(String action, Bundle extras);
    }
}
