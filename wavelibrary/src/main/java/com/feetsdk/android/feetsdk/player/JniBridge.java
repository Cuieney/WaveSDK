package com.feetsdk.android.feetsdk.player;

import android.os.Handler;

/**
 * Created by cuieney on 16/8/25.
 */
public class JniBridge {

    public native void FeetPower(String apkPath, long[] offsetAndLength);

    public native void onPlayPause(boolean play);

    public native void onCrossfader(int value);

    public native void setVolume(int value);

    public native void onFxSelect(int value);

    public native void onFxOff();

    public native void onFxValue(int value);

    public native void onChangeBpm(float value);

    public native void setTempo(float tempo, boolean masterTempo);

    public native void setBpm(float bpm);

    public native void openPathB(String apkPath, long[] offsetAndLength);

    public native void openPathA(String apkPath, long[] offsetAndLength);

    public native void openPath(String url);

    public native float getPositionPercent();

    public native void Seek(float percent);

    public native boolean isPlaying();

    public native long getDurationSeconds();

    public native long getPositonSeconds();

    public native void UpdateStatus();

    public long durationSeconds;
    public long positionSeconds;
    public float positionPercent;
    public boolean playing;


    public final Handler mHandler;
    public final Runnable mRunnable;

    private boolean isContainue = true;
    public JniBridge() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                UpdateStatus();
                if (isContainue) {
                    mHandler.postDelayed(this, 50);
                }
            }
        };
        mHandler = new Handler();
        if (isContainue) {
            mHandler.postDelayed(mRunnable, 50);
        }
    }
    public void stop(){
        isContainue = false;
        mHandler.removeCallbacks(mRunnable);
    }

    static {
        System.loadLibrary("FeetPower");
    }

}
