package com.feetsdk.android.feetsdk.musicplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by cuieney on 17/1/6.
 */
public class PlayNannyService extends Service {

    MyBinder myBinder;
    MyServiceConnection myServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        if (myBinder == null) {
            myBinder = new MyBinder();
        }
        myServiceConnection = new MyServiceConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.bindService(new Intent(this,MusicService.class), myServiceConnection, Context.BIND_IMPORTANT);

        //设置service为前台进程，避免手机休眠时系统自动杀掉该服务
        grayGuard();
        return START_STICKY;
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // 连接出现了异常断开了，LocalCastielService被杀死了
            // 启动LocalCastielService
            PlayNannyService.this.startService(new Intent(PlayNannyService.this,MusicService.class));
            PlayNannyService.this.bindService(new Intent(PlayNannyService.this,MusicService.class), myServiceConnection, Context.BIND_IMPORTANT);
        }

    }

    class MyBinder extends RemoteConnection.Stub {

        @Override
        public String getProName() throws RemoteException {
            return "RemoteService";
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return myBinder;
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
