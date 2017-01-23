package com.feetsdk.android.feetsdk.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.stepcount.IStepChange;
import com.feetsdk.android.feetsdk.stepcount.StepDetector;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by cuieney on 17/1/3.
 */
public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {
    public static final String ACTION_CMD = "com.phl.android.uamp.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    private static final String TAG = "MusicService";
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    public static final String EXTRA_CONNECTED_CAST = "com.phl.android.uamp.CAST_NAME";
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private static final int STOP_DELAY = 15000;

    public OrmHelper ormHelper;
    public LocalPlayback localPlayback;
    public PlaybackManager playbackManager;
    public MediaSessionCompat.Token sessionToken;
    public MediaSessionCompat mSessionCompat;
    public PlaybackManager.MyMediaSession sessionCallback;


//    MyBinder myBinder;
//    MyServiceConnection myServiceConnection;

    private BroadcastReceiver playerServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                handleBroadcastReceived(intent);
            } catch (IOException e) {
                e.printStackTrace();
                playbackManager.updatePlaybackState("cant play this song!!!!");
            }
        }
    };
    public PackageValidator mPackageValidator;
    public MediaNotificationManager mMediaNotificationManager;
    public SensorManager sensormanager;
    public StepDetector stepDetector;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (myBinder == null) {
//            myBinder = new MyBinder();
//        }
//        myServiceConnection = new MyServiceConnection();

        mPackageValidator = new PackageValidator(this);

        ormHelper = new OrmHelper(this);
        QueueManager queueManager = new QueueManager(ormHelper, new QueueManager.MetadataUpdateListener() {
            @Override
            public void onMetadataChanged(Music music) {
                Intent intent = new Intent(MusicProxy.ACTION_MEDIA_DATA);
                Bundle bundle = new Bundle();
                bundle.putParcelable(MusicProxy.KEY_MEDIA_DATA, music);
                intent.putExtras(bundle);
                sendBroadcast(intent);

                mSessionCompat.setMetadata(QueueManager.music2Metadata(music));
            }

            @Override
            public void onMetadataRetrieveError() {
                playbackManager.updatePlaybackState("Unable to retrieve metadata");
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                playbackManager.handlePlayRequest();
            }


        });

        localPlayback = new LocalPlayback(this);
        playbackManager = new PlaybackManager(this, queueManager, localPlayback);
        sessionCallback = playbackManager.getMediaSessionCallback();
        initMediaSession();

        sensormanager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        initPedometer();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicProxy.ACTION_NEXT_SONG);
        filter.addAction(MusicProxy.ACTION_PLAY_SONG);
        filter.addAction(MusicProxy.ACTION_PAUSE_SONG);
        filter.addAction(MusicProxy.ACTION_PREV_SONG);
        filter.addAction(MusicProxy.ACTION_STOP_MUSIC);
        filter.addAction(MusicProxy.ACTION_CONTINUE_SONG);
        filter.addAction(MusicProxy.ACTION_SET_SEEK);
        filter.addAction(MusicProxy.ACTION_SET_TEMPO);
        filter.addAction(MusicProxy.CUSTOME_ACTION_UPDATED_SONG);
        registerReceiver(playerServiceBroadcastReceiver, filter);

        sessionCallback.onPlay();



    }

    private void initPedometer() {
        stepDetector = new StepDetector(sensormanager);
        stepDetector.registerStepListener(new IStepChange() {
            @Override
            public void getStepCount(double stepcount) {
                Intent intent1 = new Intent(MusicProxy.STEP_CHANGE_STEP);
                intent1.putExtra("stepCount",stepcount);
                sendBroadcast(intent1);
            }

            @Override
            public void getCurrentBpm(int bpm) {
                Intent intent1 = new Intent(MusicProxy.STEP_CHANGE_BPM);
                intent1.putExtra("bpm",bpm);
                sendBroadcast(intent1);
            }
        });
    }


    private void initMediaSession() {
        mSessionCompat = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSessionCompat.getSessionToken());
        mSessionCompat.setActive(true);
        sessionToken = mSessionCompat.getSessionToken();

        mSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        sendToken();
        mSessionCompat.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                final String intentAction = mediaButtonEvent.getAction();
                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                    final KeyEvent event = mediaButtonEvent.getParcelableExtra(
                            Intent.EXTRA_KEY_EVENT);
                    if (event == null) {
                        return super.onMediaButtonEvent(mediaButtonEvent);
                    }
                    final int keycode = event.getKeyCode();
                    final int action = event.getAction();
                    if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                        switch (keycode) {
                            // Do what you want in here
                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                sessionCallback.onPause();
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                sessionCallback.onPlay();
                                break;
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                sessionCallback.onPlay();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                sessionCallback.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                sessionCallback.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                sessionCallback.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                sessionCallback.onSeekTo(pos);
            }

            @Override
            public void onPause() {
                super.onPause();
                sessionCallback.onPause();
            }

            @Override
            public void onCustomAction(String action, Bundle extras) {
                super.onCustomAction(action, extras);
                sessionCallback.onCustomAction(action,extras);
            }
        });

        playbackManager.updatePlaybackState(null);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }


    }

    private void sendToken() {
        Intent intent = new Intent(MusicProxy.ACTION_MEDIA_SESSION);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MusicProxy.KEY_TOKEN, sessionToken);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }


    private void handleBroadcastReceived(final Intent intent) throws IOException {
        switch (intent.getAction()) {
            case MusicProxy.ACTION_PLAY_SONG:
                sessionCallback.onPlay();
                break;
            case MusicProxy.ACTION_NEXT_SONG:
                sessionCallback.onSkipToNext();
                break;
            case MusicProxy.ACTION_PAUSE_SONG:
                sessionCallback.onPause();
                break;
            case MusicProxy.ACTION_CONTINUE_SONG:
                sessionCallback.onPlay();
                break;
            case MusicProxy.ACTION_PREV_SONG:
                sessionCallback.onSkipToPrevious();
                break;
            case MusicProxy.ACTION_STOP_MUSIC:
                sessionCallback.onStop();
                break;

            case MusicProxy.ACTION_SET_TEMPO:
                float floatExtra = intent.getFloatExtra(MusicProxy.KEY_TEMPO, 1);
                sessionCallback.setTempo(floatExtra);
                break;
            case MusicProxy.ACTION_SET_SEEK:
                long intExtra = intent.getIntExtra(MusicProxy.KEY_SEEK, 0);
                sessionCallback.onSeekTo(intExtra);
                break;

            case MusicProxy.CUSTOME_ACTION_UPDATED_SONG:
                sessionCallback.onCustomAction(intent.getStringExtra(MusicProxy.KEY_CUSTOM_ACTION),intent.getExtras());
                break;

        }
    }


    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (!isConnected) {
//            this.bindService(new Intent(this, PlayNannyService.class), myServiceConnection, Context.BIND_IMPORTANT);
        }

        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    playbackManager.handlePauseRequest();
                }
            }
        } else {
            MediaButtonReceiver.handleIntent(mSessionCompat, startIntent);
        }
        sendToken();

        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service is being killed, so make sure we release our resources
        playbackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        unregisterReceiver(playerServiceBroadcastReceiver);

        if (sessionCallback != null) {
            sessionCallback.onStop();
        }
        mSessionCompat.release();
        sensormanager = null;
        if (stepDetector != null) {
            stepDetector.unregisterStepListener();
            stepDetector = null;
            sensormanager = null;
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        ormHelper.closeDb();
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return null. No further calls will
            // be made to other media browsing methods.
            Logger.e(TAG, "OnGetRoot: IGNORING request from untrusted package "
                    + clientPackageName);
            return null;
        }

        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public void onPlaybackStart() {
        if (!mSessionCompat.isActive()) {
            mSessionCompat.setActive(true);
        }

        mDelayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onPlaybackStop() {
//        unbindService(myServiceConnection);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSessionCompat.setPlaybackState(newState);
        Intent intent = new Intent(MusicProxy.ACTION_PLAYBACK_STATE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MusicProxy.KEY_PLAYBACK_STATE,newState);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.playbackManager.getPlayback() != null) {
                if (service.playbackManager.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
    }



    private boolean isConnected;

//    class MyServiceConnection implements ServiceConnection {
//
//        @Override
//        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//            isConnected = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            // 连接出现了异常断开了，RemoteService被杀掉了
//            // 启动RemoteCastielService
//            isConnected = false;
//            MusicService.this.startService(new Intent(MusicService.this, PlayNannyService.class));
//            MusicService.this.bindService(new Intent(MusicService.this, PlayNannyService.class),
//                    myServiceConnection, Context.BIND_IMPORTANT);
//        }
//
//    }
//
//    class MyBinder extends RemoteConnection.Stub {
//
//        @Override
//        public String getProName() throws RemoteException {
//            return "musicservice";
//        }
//
//    }
//
//    @Override
//    public IBinder onBind(Intent arg0) {
//        return myBinder;
//    }

}
