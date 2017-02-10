package com.feetsdk.android.feetsdk.musicplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;

import com.feetsdk.android.R;
import com.feetsdk.android.feetsdk.player.JniBridge;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cuieney on 16/8/25.
 */
public class FeetPlayer implements ISuperPowerPlayer {
    private String samplerateString;
    private String buffersizeString;


    //设置播放完成回调
    private Handler mHandler;
    private boolean lock;

    //监听回调
    private OnCompletionCallBack onComplicationListener;
    private OnErrorListener onErrorListener;
    private OnPreparedListener onPreparedListener;


    //控制器
    //判断当前播放器
    private boolean isPlayerA;
    //混音是否成功
    private boolean remixSuccess = true;
    //跳过混音过程
    private boolean skipRemix;

    //音量控制
    //volA
    private int volA;
    private int volB;


    private ExecutorService fixedThreadPool;

    private JniBridge jniBridge;

    private static EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean isListener = true;

    public FeetPlayer(Context context) {
        jniBridge = new JniBridge();
        mEventHandler = new EventHandler(this);

        fixedThreadPool = Executors.newFixedThreadPool(5);
        if (Build.VERSION.SDK_INT >= 17)

        {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            samplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            buffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }

        if (samplerateString == null) samplerateString = "44100";
        if (buffersizeString == null) buffersizeString = "512";

        AssetFileDescriptor fd0 = context.getResources().openRawResourceFd(R.raw.bpm);
        AssetFileDescriptor fd1 = context.getResources().openRawResourceFd(R.raw.bpm);

        long[] params = {
                0,
                0,
                0,
                0,
                Integer.parseInt(samplerateString),
                Integer.parseInt(buffersizeString)
        };

        try

        {
            fd0.getParcelFileDescriptor().close();
            fd1.getParcelFileDescriptor().close();
        } catch (
                IOException e
                )

        {
            android.util.Log.d("", "Close error.");
        }

        jniBridge.FeetPower(context.getPackageResourcePath(), params);


        //监听播放进度
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = (int) (jniBridge.getPositionPercent() * 100);
                if (progress == 95 && !lock) {
                    lock = true;
                    if (mEventHandler != null) {
                        mEventHandler.onComplicationListener();
                    }
                } else if (progress == 96) {
                    lock = false;
                }
                if (isListener) {
                    mHandler.postDelayed(this, 500);
                } else {
                    jniBridge = null;
                }
            }
        };
        mHandler = new Handler();
        if (isListener) {
            mHandler.postDelayed(mRunnable, 500);
        }
    }

    public void setOnComplicationListener(OnCompletionCallBack onComplicationListener) {
        this.onComplicationListener = onComplicationListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    @Override
    public void onPlayPause(final boolean play) {
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                jniBridge.onPlayPause(play);
            }
        });


    }


    public synchronized void play(final File file) {
        final String path = file.getAbsolutePath();
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (isPlayerA) {
                    long[] params = {
                            0,
                            file.length(),
                            Integer.parseInt(samplerateString),
                            Integer.parseInt(buffersizeString)
                    };
                    jniBridge.openPathB(path, params);
                    jniBridge.onCrossfader(100);
                    isPlayerA = false;
                    handler.sendEmptyMessage(1);
                } else {
                    long[] params = {
                            0,
                            file.length(),
                            Integer.parseInt(samplerateString),
                            Integer.parseInt(buffersizeString)
                    };
                    jniBridge.openPathA(path, params);
                    jniBridge.onCrossfader(0);
                    isPlayerA = true;
                    handler.sendEmptyMessage(1);
                }
            }
        });


    }

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mEventHandler != null) {
                mEventHandler.onPreparedListener();
            }
        }
    };


    public synchronized void remixPlay(final File file) {
        final String path = file.getAbsolutePath();
        if (!remixSuccess) {
            skipRemix(!isPlayerA);
        }
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (isPlayerA) {
                    long[] params = {
                            0,
                            file.length(),
                            Integer.parseInt(samplerateString),
                            Integer.parseInt(buffersizeString)
                    };
                    jniBridge.openPathB(path, params);
                    remixSuccess = false;
                    remixSound(isPlayerA);
                    isPlayerA = false;
                } else {
                    long[] params = {
                            0,
                            file.length(),
                            Integer.parseInt(samplerateString),
                            Integer.parseInt(buffersizeString)
                    };
                    jniBridge.openPathA(path, params);
                    remixSuccess = false;
                    remixSound(isPlayerA);
                    isPlayerA = true;
                }
            }
        });
    }

    private void remixSound(boolean isPlayerA) {
        if (isPlayerA) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (volA != 100 && !remixSuccess) {
                        SystemClock.sleep(600);
                        jniBridge.onCrossfader(volA += 10);
                    }

                    volB = 100;
                    remixSuccess = true;
                    handler.sendEmptyMessage(1);
                }
            });
        } else {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (volB != 0 && !remixSuccess) {
                        SystemClock.sleep(600);
                        jniBridge.onCrossfader(volB -= 10);
                    }
                    remixSuccess = true;
                    volA = 0;
                    handler.sendEmptyMessage(1);
                }
            });
        }
    }

    private void skipRemix(boolean isPlayerA) {
        if (!isPlayerA) {
            remixSuccess = true;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        SystemClock.sleep(100);
                        jniBridge.onCrossfader(0);
                    }
                }
            });

        } else {
            remixSuccess = true;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        SystemClock.sleep(100);
                        jniBridge.onCrossfader(100);
                    }
                }
            });

        }
    }

    @Override
    public void play(final String path, boolean isRemix) {
        File file = new File(path);
        if (isRemix) {
            if (!remixSuccess) {
                skipRemix(!isPlayerA);
            }
            if (file.exists()) {
                remixPlay(file);
            } else {
                if (mEventHandler != null) {
                    mEventHandler.onErrorListener();
                }
            }
        } else {
            if (file.exists()) {
                play(file);
            } else {
                if (mEventHandler != null) {
                    mEventHandler.onErrorListener();
                }
            }
        }


    }


    @Override
    public void setTempo(float rate) {
        jniBridge.setTempo(rate, true);
    }

    @Override
    public void setBpm(float bpm) {
        jniBridge.setBpm(bpm);
    }

    @Override
    public void setSeek(int percent) {
        jniBridge.Seek((float) (percent) / 100.0f);
    }

    @Override
    public boolean isPlaying() {
        return jniBridge.playing;
    }

    @Override
    public long getDurationSeconds() {
        return jniBridge.durationSeconds;
    }

    @Override
    public long getPositonSeconds() {
        return jniBridge.positionSeconds;
    }

    private class EventHandler {
        private FeetPlayer mFp;

        public EventHandler(FeetPlayer fp) {
            mFp = fp;
        }

        public void onPreparedListener() {
            if (onPreparedListener != null) {
                onPreparedListener.onPrepared(mFp);
            }
        }

        public void onComplicationListener() {
            if (onComplicationListener != null) {
                onComplicationListener.onCompletionListener(mFp);
            }
        }

        public void onErrorListener() {
            if (onErrorListener != null) {
                onErrorListener.onErrorListener(mFp, 404, 404);
            }
        }
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE, FeetPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void reset() {
//        onPlayPause(false);
        stayAwake(false);

        // make sure none of the listeners get called anymore
    }

    public void release() {
        stayAwake(false);
        onComplicationListener = null;
        onErrorListener = null;
        onPreparedListener = null;
        fixedThreadPool.shutdown();
        if (jniBridge != null) {
            jniBridge.stop();
            isListener = false;
        }
    }


    public void setVolume(final int volume) {
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                jniBridge.setVolume(volume);
            }
        });
    }

    public interface OnCompletionCallBack {
        void onCompletionListener(FeetPlayer fp);
    }

    public interface OnErrorListener {
        void onErrorListener(FeetPlayer fp, int what, int extra);
    }

    public interface OnPreparedListener {
        void onPrepared(FeetPlayer fp);
    }
}
