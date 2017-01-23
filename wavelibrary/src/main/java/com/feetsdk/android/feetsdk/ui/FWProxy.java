package com.feetsdk.android.feetsdk.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feetsdk.android.FeetSdk;
import com.feetsdk.android.R;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.download.DownloadControler;
import com.feetsdk.android.feetsdk.download.IUpdateProgressCallBack;
import com.feetsdk.android.feetsdk.entity.DownloadProgress;
import com.feetsdk.android.feetsdk.entity.UpdateProgress;
import com.feetsdk.android.feetsdk.musicplayer.MusicController;
import com.feetsdk.android.feetsdk.musicplayer.OnMediaControllerListener;
import com.feetsdk.android.feetsdk.musicplayer.OnMediaStateUpdatedListener;
import com.feetsdk.android.feetsdk.musicplayer.OnMusicChangeListener;

/**
 * Created by cuieney on 17/1/10.
 */
public class FWProxy implements View.OnClickListener {
    private FloatWindow mFloatWindow;
    public MediaControllerCompat mediaSessionControls;
    private MusicController mMusicCtl;
    private DownloadControler mDownloadCtl;
    private Context context;
    private View mMenuLayout;
    private View mPlayerLayout;
    public CircularMusicProgressBar mCircularProgress;
    private ImageView mDownloadSetting;
    private ImageView mDownLoadPlay;
    private ImageView mDownloadClose;
    private ProgressBar mLineProgress;
    private ImageView mPlayerLock;
    private ImageView mPlayerStop;
    private ImageView mPlayerPause;
    private ImageView mPlayerPlay;
    private ImageView mPlayerNext;
    private TextView mPlayerName;
    private ImageView mPlayerClose;
    private ImageView mPlayerSub;
    private TextView mPlayerBpm;
    private ImageView mPlayerAdd;
    private View downloadContainer;
    private View playerContainer;
    private View lockContainer;

    private AnimatorSet mRightOutSet; // 右出动画
    private AnimatorSet mLeftInSet; // 左入动画

    private boolean mIsShowBack;

    private boolean isLocked = true;

    private Music mCurrentMsc;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mPlayerName.setText(((String) msg.obj));
            } else if (msg.what == 2) {
                mPlayerNext.setVisibility(View.VISIBLE);
                mPlayerName.setVisibility(View.GONE);
                mPlayerPause.setVisibility(View.VISIBLE);
            }

        }
    };


    public FWProxy(Context context) {
        this.context = context.getApplicationContext();
        init();
    }

    private void init() {
        mMenuLayout = LayoutInflater.from(context).inflate(R.layout.fw_menu_layout, null);
        mPlayerLayout = LayoutInflater.from(context).inflate(R.layout.fw_layout, null);
        initView();
        mMusicCtl = new MusicController(context);
        mDownloadCtl = FeetSdk.getDownloadControler(context);
        initPlayer();
        updateMusicDownload();

        mFloatWindow = new FloatWindow(context);
        mFloatWindow.setFloatView(mMenuLayout);
        mFloatWindow.setPlayerView(mPlayerLayout);

    }

    private void initView() {
        mCircularProgress = ((CircularMusicProgressBar) findViewById(mMenuLayout, R.id.progressBar));

        downloadContainer = findViewById(mPlayerLayout, R.id.download_container);
        playerContainer = findViewById(mPlayerLayout, R.id.player_container);
        lockContainer = findViewById(mPlayerLayout, R.id.lock_container);
        lockContainer.setVisibility(View.GONE);
        mDownloadSetting = ((ImageView) findViewById(mPlayerLayout, R.id.menu));
        mDownLoadPlay = ((ImageView) findViewById(mPlayerLayout, R.id.download_play));
        mDownloadClose = ((ImageView) findViewById(mPlayerLayout, R.id.download_close));
        mLineProgress = ((ProgressBar) findViewById(mPlayerLayout, R.id.progress_line));

        mPlayerLock = ((ImageView) findViewById(mPlayerLayout, R.id.lock));
        mPlayerStop = ((ImageView) findViewById(mPlayerLayout, R.id.stop));
        mPlayerPause = ((ImageView) findViewById(mPlayerLayout, R.id.pause));
        mPlayerPlay = ((ImageView) findViewById(mPlayerLayout, R.id.play));
        mPlayerNext = ((ImageView) findViewById(mPlayerLayout, R.id.next));
        mPlayerName = ((TextView) findViewById(mPlayerLayout, R.id.name));
        mPlayerClose = ((ImageView) findViewById(mPlayerLayout, R.id.close));
        mPlayerSub = ((ImageView) findViewById(mPlayerLayout, R.id.sub));
        mPlayerBpm = ((TextView) findViewById(mPlayerLayout, R.id.bpm));
        mPlayerAdd = ((ImageView) findViewById(mPlayerLayout, R.id.add));


        initListener();

    }

    private void initPlayer() {
        mMusicCtl.registerMusicChangeListener(new OnMusicChangeListener() {
            @Override
            public void onMusicChange(Music info) {
                Message message = Message.obtain();
                message.obj = info.getSongName();
                message.what = 1;
                handler.sendMessage(message);
                if (mCurrentMsc == null) {
                    mCurrentMsc = info;
//                    updateNextStatue();
                }
                if (!mCurrentMsc.getSongId().equals(info.getSongId())) {
                    mCurrentMsc = info;
                    updateNextStatue();
                }

            }
        });

        mMusicCtl.registerMediaControllerChangeListener(new OnMediaControllerListener() {
            @Override
            public void onMediaControllerChange() {
                mediaSessionControls = mMusicCtl.getMediaControls();
            }
        });

        mMusicCtl.registerMeidaUpdatedListener(new OnMediaStateUpdatedListener() {
            @Override
            public void OnMediaStateUpdated(PlaybackStateCompat playbackStateCompat) {
                if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    updatePlayStatue();
                } else if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED) {
                    updatePauseStatue();
                }
            }
        });
    }

    private void updateMusicDownload() {
        DownloadProgress currentProgress = mDownloadCtl.getCurrentProgress();
        int downloadProgress = currentProgress.getDownloadPorgress();
        mCircularProgress.setValue(downloadProgress);
        mLineProgress.setProgress(downloadProgress);
        mDownloadCtl.startDownload(new IUpdateProgressCallBack() {
            @Override
            public void progress(UpdateProgress progress) {
                mCircularProgress.setValue(progress.getPrecent());
                mLineProgress.setProgress(progress.getPrecent());
            }
        }, EventType.DOWNLOAD_GET_MUSIC);
    }


    private void initListener() {
        mDownLoadPlay.setOnClickListener(this);
        mPlayerPause.setOnClickListener(this);
        mPlayerPlay.setOnClickListener(this);
        mPlayerStop.setOnClickListener(this);
        mPlayerNext.setOnClickListener(this);
        mPlayerLock.setOnClickListener(this);
        mPlayerSub.setOnClickListener(this);
        mPlayerAdd.setOnClickListener(this);
        mDownloadClose.setOnClickListener(this);
        mPlayerClose.setOnClickListener(this);
        mDownloadSetting.setOnClickListener(this);


        setAnimators(); // 设置动画
        setCameraDistance(); // 设置镜头距离
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.download_play) {
            showPlayer();
        }

        if (i == R.id.pause) {
            mMusicCtl.onPause();
//            updatePauseStatue();
        }

        if (i == R.id.play) {
            mMusicCtl.onContinue();
//            updatePlayStatue();
        }

        if (i == R.id.stop) {
            showDownload();
        }

        if (i == R.id.next) {
            mMusicCtl.onSkipToNext();
        }

        if (i == R.id.lock) {
            updateLockedStatue();
        }

        if (i == R.id.sub) {
            updateBpmStatue(false);
        }

        if (i == R.id.add) {
            updateBpmStatue(true);
        }

        if (i == R.id.close) {
            mFloatWindow.turnMini();
        }
        if (i == R.id.download_close) {
            mFloatWindow.turnMini();
        }
    }


    private void updateNextStatue() {
        mPlayerNext.setVisibility(View.GONE);
        mPlayerName.setVisibility(View.VISIBLE);
        mPlayerPause.setVisibility(View.GONE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(6000);
                Message message = Message.obtain();
                message.what = 2;
                handler.sendMessage(message);
            }
        }).start();
    }


    private void updateBpmStatue(boolean isAdd) {
        int value = Integer.parseInt(mPlayerBpm.getText().toString().substring(0, 3));
        if (isAdd) {
            value += 5;
        } else {
            value -= 5;
        }
        if (mCurrentMsc != null && mMusicCtl != null) {
            if (value >= 120 && value <= 220) {
                mPlayerBpm.setText(value + "BPM");
                mMusicCtl.setTempo(value);
            } else {
                mPlayerBpm.setText("120BPM");
                mMusicCtl.setTempo(120);
            }
        }
    }

    private void updatePauseStatue() {

        mPlayerStop.setVisibility(View.VISIBLE);
        mPlayerLock.setVisibility(View.GONE);
        mPlayerNext.setVisibility(View.GONE);
        mPlayerPlay.setVisibility(View.VISIBLE);
        mPlayerName.setVisibility(View.VISIBLE);
        mPlayerPause.setVisibility(View.GONE);
    }

    private void updatePlayStatue() {

        mPlayerStop.setVisibility(View.GONE);
        mPlayerLock.setVisibility(View.VISIBLE);
        mPlayerNext.setVisibility(View.VISIBLE);
        mPlayerPlay.setVisibility(View.GONE);
        mPlayerName.setVisibility(View.GONE);
        mPlayerPause.setVisibility(View.VISIBLE);
    }


    private void showPlayer() {
        mMusicCtl.onPlay();
        mMusicCtl.onCustomAction("updated_song", new Bundle());
        flipCard();
    }

    private void showDownload() {
        flipCard();
        if (mediaSessionControls != null &&
                (mediaSessionControls.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)) {
            mMusicCtl.onPause();
        }
        updateMusicDownload();
    }

    public void show() {
        if (mFloatWindow != null) {
            mFloatWindow.show();
        }
    }

    public void dismiss() {
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
        }
    }

    public void remove() {
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
        }
        if (mediaSessionControls != null &&
                (mediaSessionControls.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)) {
            mMusicCtl.onPause();
        }

        mMusicCtl.onStop();
    }

    private View findViewById(View view, int id) {
        return view.findViewById(id);
    }


    // 设置动画
    private void setAnimators() {
        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.anim_out);
        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.anim_in);

        // 设置点击事件
        mRightOutSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                downloadContainer.setClickable(false);
                playerContainer.setClickable(true);
            }
        });
        mLeftInSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                downloadContainer.setClickable(true);
                playerContainer.setClickable(false);

                if (mIsShowBack) {
                    downloadContainer.setVisibility(View.GONE);
                    playerContainer.setVisibility(View.VISIBLE);
                } else {
                    downloadContainer.setVisibility(View.VISIBLE);
                    playerContainer.setVisibility(View.GONE);
                }
            }

        });
    }

    // 改变视角距离, 贴近屏幕
    private void setCameraDistance() {
        int distance = 16000;
        float scale = context.getResources().getDisplayMetrics().density * distance;
        downloadContainer.setCameraDistance(scale);
        playerContainer.setCameraDistance(scale);
    }

    // 翻转卡片
    public void flipCard() {
        // 正面朝上
        if (!mIsShowBack) {
            mRightOutSet.setTarget(downloadContainer);
            mLeftInSet.setTarget(playerContainer);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = true;

        } else { // 背面朝上
            mRightOutSet.setTarget(playerContainer);
            mLeftInSet.setTarget(downloadContainer);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = false;

        }
    }

    private void updateLockedStatue() {
        if (isLocked) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                turnOnAnimator(lockContainer);
            } else {
                lockContainer.setVisibility(View.VISIBLE);
                isLocked = false;

            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                turnOffAnimator(lockContainer);
            } else {
                lockContainer.setVisibility(View.GONE);
                isLocked = true;
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void turnOffAnimator(final View container) {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                container,
                0,
                0,
                (float) Math.hypot(container.getWidth(), container.getHeight()),
                0
        );
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                container.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(View.GONE);
                container.setEnabled(true);
                isLocked = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private boolean isFirst = true;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void turnOnAnimator(final View container) {
        if (isFirst) {
            container.setVisibility(View.VISIBLE);
            isLocked = false;
            isFirst = false;
            return;
        }
        Animator animator = ViewAnimationUtils.createCircularReveal(
                container,
                0,
                0,
                0,
                (float) Math.hypot(container.getWidth(), container.getHeight())
        );
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                container.setVisibility(View.VISIBLE);
                container.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                container.setEnabled(true);
                isLocked = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }


}
