package com.feetsdk.android.feetsdk.stepcount;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.feetsdk.android.common.utils.Logger;

import java.util.Calendar;

/**
 * Created by cuieney on 16/12/21.
 *
 */
@SuppressLint("Registered")
public class StepService extends Service{
    private StepDetector stepDetector;
    public SensorManager sensormanager;
    private final IBinder mBinder = new StepBinder();
    public PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            public void run() {
                init();
            }
        }).start();

    }

    private void init() {
        sensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepDetector = new StepDetector(sensormanager);
        getLock(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        startForeground(1235, new Notification());
        if (sensormanager == null && stepDetector == null) {
            init();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (stepDetector != null) {
            stepDetector.unregisterStepListener();
            stepDetector = null;
            sensormanager = null;
        }
        stopForeground(true);
        Logger.d("StepService onDestroy");
    }


    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    public void reigisterStepListener(IStepChange change){
        if (stepDetector != null) {
            stepDetector.registerStepListener(change);
        }
    }

    synchronized private PowerManager.WakeLock getLock(Context context) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld())
                mWakeLock.release();
            mWakeLock = null;
        }

        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    StepService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            } else {
                mWakeLock.acquire(300000);
            }
        }
        return (mWakeLock);
    }



}
