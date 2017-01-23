/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.feetsdk.android.feetsdk.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.feetsdk.android.R;


/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.phl.android.uamp.pause";
    public static final String ACTION_PLAY = "com.phl.android.uamp.play";
    public static final String ACTION_PREV = "com.phl.android.uamp.prev";
    public static final String ACTION_NEXT = "com.phl.android.uamp.next";
    public static final String ACTION_STOP_CASTING = "com.phl.android.uamp.stop_cast";

    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;

    private final PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    private boolean mStarted = false;

    public MediaNotificationManager(MusicService service) throws RemoteException {
        mService = service;
        updateSessionToken();

        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary,
                Color.DKGRAY);

        mNotificationManager = NotificationManagerCompat.from(service);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_STOP_CASTING:
                Intent i = new Intent(context, MusicService.class);
                i.setAction(MusicService.ACTION_CMD);
                i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
                mService.startService(i);
                break;
            default:
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
//        Intent openUI = new Intent(mService, MusicPlayerActivity.class);
//        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
//        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
//        }
        return PendingIntent.getActivity(mService, REQUEST_CODE, null,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            mPlaybackState = state;
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            try {
                updateSessionToken();
            } catch (RemoteException e) {
            }
        }
    };

    private Notification createNotification() {
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
//        int playPauseButtonPosition = 0;
//        // If skip to previous action is enabled
//        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
//            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp, "上一首", mPreviousIntent);
//
//            // If there is a "skip to previous" button, the play/pause button will
//            // be the second one. We need to keep track of it, because the MediaStyle notification
//            // requires to specify the index of the buttons (actions) that should be visible
//            // when in compact view.
//            playPauseButtonPosition = 1;
//        }
//
//        addPlayPauseAction(notificationBuilder);
//
//        // If skip to next action is enabled
//        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
//            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp, "下一首", mNextIntent);
//        }
//
//        MediaDescriptionCompat description = mMetadata.getDescription();
//
//        String fetchArtUrl = null;
//        Bitmap art = null;
//        if (description.getIconUri() != null) {
//            // This sample assumes the iconUri will be a valid URL formatted String, but
//            // it can actually be any valid Android Uri formatted String.
//            // async fetch the album art icon
//            String artUrl = description.getIconUri().toString();
//            Logger.e(artUrl);
//            art = AlbumArtCache.getInstance().getBigImage(artUrl);
//            if (art == null) {
//                fetchArtUrl = artUrl;
//                // use a placeholder art while the remote art is being downloaded
//                art = BitmapFactory.decodeResource(mService.getResources(),
//                        R.drawable.ic_default_art);
//            }
//        }
//        RemoteViews notiCollapsedView = new RemoteViews(mService.getPackageName(),
//                R.layout.notification);
//        notificationBuilder.setContent(notiCollapsedView);
//        Bitmap bitmap = mMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
//        notificationBuilder
////                .setStyle(new NotificationCompat.MediaStyle()
////                        .setShowActionsInCompactView(
////                                new int[]{playPauseButtonPosition})  // show only play/pause in compact view
////                        .setMediaSession(mSessionToken))
////                .setColor(mNotificationColor)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
////                .setUsesChronometer(true)
////                .setContentIntent(createContentIntent(description))
////                .setContentTitle(description.getTitle())
////                .setContentText(description.getSubtitle())
////                .setLargeIcon(bitmap);
//
//
//        if (mController != null && mController.getExtras() != null) {
//            String castName = mController.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
//            if (castName != null) {
//                String castInfo = "Casting to %1$s";
//                notificationBuilder.setSubText(castInfo);
//                notificationBuilder.addAction(R.drawable.ic_close_black_24dp, "停止", mStopCastIntent);
//            }
//        }
//
//        setNotificationPlaybackState(notificationBuilder);
//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
//        }
//


        return getNotification();
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = "暂停";
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = "播放";
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mPlaybackState == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            Log.d("oye", "updateNotificationPlaybackState. updating playback position to "+
                    (System.currentTimeMillis() - mPlaybackState.getPosition())+ " seconds");
//            builder
//                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition()*1000)
//                    .setShowWhen(true)
//                    .setUsesChronometer(true);
        } else {
            Log.d("oye", "updateNotificationPlaybackState. hiding playback position");
//            builder
//                    .setWhen(0)
//                    .setShowWhen(false)
//                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final Notification builder) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (mMetadata != null && mMetadata.getDescription().getIconUri() != null &&
                        mMetadata.getDescription().getIconUri().toString().equals(artUrl)) {
                    // If the media is still the same, update the notification:
                    builder.contentView.setImageViewBitmap(R.id.image,bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder);
                }
            }
        });
    }


    private Notification getNotification() {
        RemoteViews remoteViews;
        final String albumName = getAlbumName();
        final String artistName = getArtistName();
        final boolean isPlaying = isPlaying();

        remoteViews = new RemoteViews(mService.getPackageName(), R.layout.notification);
        Log.e("playing","get notification start 3.1");
        String text = TextUtils.isEmpty(albumName) ? artistName : artistName + " - " + albumName;
        remoteViews.setTextViewText(R.id.title, albumName);
        remoteViews.setTextViewText(R.id.text, text);

        //此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
        remoteViews.setImageViewResource(R.id.iv_pause, isPlaying ? R.drawable.note_btn_pause : R.drawable.note_btn_play);
        if (isPlaying) {
            remoteViews.setOnClickPendingIntent(R.id.iv_pause, mPauseIntent);
        }else{
            remoteViews.setOnClickPendingIntent(R.id.iv_pause, mPlayIntent);
        }
        //remoteView.setInt(R.id.iv_pause, "setBackgroundResource", R.color.your_color);

        remoteViews.setOnClickPendingIntent(R.id.iv_next, mNextIntent);

        remoteViews.setOnClickPendingIntent(R.id.iv_stop, mStopCastIntent);

        final Intent nowPlayingIntent = new Intent();
        nowPlayingIntent.setComponent(new ComponentName("com.phl.wavesdk","com.phl.wavesdk.MainActivity"));
//        nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent click = PendingIntent.getActivity(mService,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);

//        String fetchArtUrl = null;
//        Bitmap art = null;
//        if (mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) != null) {
//            // This sample assumes the iconUri will be a valid URL formatted String, but
//            // it can actually be any valid Android Uri formatted String.
//            // async fetch the album art icon
//            String artUrl = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
//            Logger.e(artUrl+" ==");
//            art = AlbumArtCache.getInstance().getBigImage(artUrl);
//            if (art == null) {
//                fetchArtUrl = artUrl;
//                // use a placeholder art while the remote art is being downloaded
//                art = BitmapFactory.decodeResource(mService.getResources(),
//                        R.drawable.ic_default_art);
//            }
//        }

        remoteViews.setImageViewBitmap(R.id.image, BitmapHelper.scaleBitmap(mMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART),100,100));
        if(mNotification == null){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mService).setContent(remoteViews)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(click);
            mNotification = builder.build();
        }else {
            mNotification.contentView = remoteViews;
        }
//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, mNotification);
//        }

        return mNotification;
    }
    private Notification mNotification;
    private String getAlbumName() {
        return mMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    public String getArtistName() {
        return mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    public boolean isPlaying() {
        return mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }
}
