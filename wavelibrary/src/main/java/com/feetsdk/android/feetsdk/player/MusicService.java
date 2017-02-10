package com.feetsdk.android.feetsdk.player;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/11/21.
 */
@SuppressLint("Registered")
public class MusicService extends Service {
    IMusicServiceHandler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return handler.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new MusicServiceHandler(new WeakReference<>(this));
    }

    @Override
    public void onDestroy() {
        handler.onDestroy();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.onStartCommand(intent, flags, startId);
        grayGuard();
        return START_STICKY;
    }


    private void grayGuard() {
        if (Build.VERSION.SDK_INT < 18) {
            //API < 18 ，此方法能有效隐藏Notification上的图标
            startForeground(1235, new Notification());
        } else {
            Intent innerIntent = new Intent(this, DaemonInnerService.class);
            startService(innerIntent);
            startForeground(1235, new Notification());
        }
    }

    @SuppressLint("Registered")
    public static class DaemonInnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(1235, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

}
