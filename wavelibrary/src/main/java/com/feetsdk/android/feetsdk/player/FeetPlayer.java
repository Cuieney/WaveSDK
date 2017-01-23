package com.feetsdk.android.feetsdk.player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import com.feetsdk.android.R;
import com.feetsdk.android.common.utils.LocalPathResolver;

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
    private CompletionCallBack onComplicationListener;
    private ErrorListener onErrorListener;


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
    public FeetPlayer(Context context) {
        jniBridge = new JniBridge();
        fixedThreadPool = Executors.newFixedThreadPool(5);
        if (Build.VERSION.SDK_INT >= 17) {
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

        try {
            fd0.getParcelFileDescriptor().close();
            fd1.getParcelFileDescriptor().close();
        } catch (IOException e) {
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
                    if (onComplicationListener != null) {
                        onComplicationListener.onCompletionListener();
                    }
                } else if (progress == 96) {
                    lock = false;
                }
                mHandler.postDelayed(this, 500);
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 500);
    }

    public void setOnComplicationListener(CompletionCallBack onComplicationListener) {
        this.onComplicationListener = onComplicationListener;
    }

    public void setOnErrorListener(ErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
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
        final String path = LocalPathResolver.getDir() + file.getName();
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
                }else {
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

    private void remixSound(boolean isPlayerA){
        if (isPlayerA) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (volA != 100 && !remixSuccess) {
                        SystemClock.sleep(600);
                        jniBridge.onCrossfader(volA += 10);
                    }
                    volB = 100;
                    remixSuccess  = true;
                }
            });
        }else {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (volB != 0 && !remixSuccess) {
                        SystemClock.sleep(600);
                        jniBridge.onCrossfader(volB -= 10);
                    }
                    remixSuccess = true;
                    volA = 0;
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

        }else{
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
    public void play(String path, boolean isRemix) {
        if (!remixSuccess) {
            skipRemix(!isPlayerA);
//            return;
        }


        File file = new File(path);
        if (file.exists()) {
            play(file);
        }

    }


    @Override
    public void setTempo(float rate) {
        jniBridge.setTempo(rate,true);
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
        return jniBridge.isPlaying();
    }


}
