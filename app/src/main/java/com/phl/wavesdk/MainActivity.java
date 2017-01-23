package com.phl.wavesdk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.download.DownloadControler;
import com.feetsdk.android.feetsdk.musicplayer.MusicController;
import com.feetsdk.android.feetsdk.ui.FwController;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public TextView progress, title;
    public Button start, next, prev, pause, clean;
    public ImageView imageView;
    public MusicController musicController;
    private String TAG = "MainActivity";
    private Context context;
    private Dialog dialog;
    public FwController feetUiController;
    public Random random;
    public int mWidth;
    public int mHeight;
    public int dHeight;
    public int dWidth;

    private View container;
    public View bezier;
    public View play;
    public DownloadControler downloadControler;

    private Music currentSongs;
    public TextView bpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate: ");
        this.context = this;
        progress = ((TextView) findViewById(R.id.progress));
        title = ((TextView) findViewById(R.id.title));
        bpm = ((TextView) findViewById(R.id.bpm));
        start = ((Button) findViewById(R.id.start));
        next = ((Button) findViewById(R.id.next));
        prev = ((Button) findViewById(R.id.prev));
        pause = ((Button) findViewById(R.id.pause));
        imageView = ((ImageView) findViewById(R.id.img));
        play = findViewById(R.id.play);
        container = findViewById(R.id.download_container);
        bezier = findViewById(R.id.bezier);
//        feetUiController = FeetSdk.getFeetUiController(this);
        startService(new Intent(MainActivity.this, Service1.class));
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feetUiController != null) {
                    feetUiController.show();
                }
            }
        });

//        downloadControler = FeetSdk.getDownloadControler(this);
//        downloadControler.startDownload(new IUpdateProgressCallBack() {
//            @Override
//            public void progress(final UpdateProgress progress1) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progress.setText(progress1.getPrecent() + "");
//                    }
//                });
//            }
//        }, EventType.DOWNLOAD_GET_MUSIC);
//
//
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                musicController.onPlay();
//            }
//        });
////
//        play.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                musicController.onContinue();
//            }
//        });
//
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                musicController.onSkipToNext();
//            }
//        });
//
//        bezier.setOnClickListener(new View.OnClickListener() {
//            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onClick(final View oval) {
////                startAnim(v);
//                container.setVisibility(View.VISIBLE);
//                Animator animator = ViewAnimationUtils.createCircularReveal(
//                        container,
//                        0,
//                        0,
//                        0,
//                        (float) Math.hypot(container.getWidth(), container.getHeight()));
//
//
//                animator.setInterpolator(new DecelerateInterpolator());
//                animator.setDuration(1500);
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        oval.setVisibility(View.GONE);
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//
//
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//                animator.start();
//            }
//        });
//
//
//        container.setOnClickListener(new View.OnClickListener() {
//            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onClick(final View oval) {
////                startAnim(v);
//
//                Animator animator = ViewAnimationUtils.createCircularReveal(
//                        oval,
//                        oval.getWidth() / 2,
//                        oval.getHeight() / 2,
//                        oval.getWidth(),
//                        0
//
//                );
//                animator.setInterpolator(new DecelerateInterpolator());
//                animator.setDuration(1500);
//                animator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        oval.setVisibility(View.GONE);
//                        bezier.setVisibility(View.VISIBLE);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//                    }
//                });
//                animator.start();
//
//            }
//        });
//        prev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                musicController.onSkipToPrevious();
//            }
//        });
//
//        pause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                musicController.onPause();
//            }
//        });
//
//
//        random = new Random();
//        Drawable red = ContextCompat.getDrawable(this, R.drawable.down);
//        dHeight = red.getIntrinsicHeight() / 2;
//        dWidth = red.getIntrinsicWidth() / 2;
    }

    private void startAnim(View imageView) {
        AnimatorSet finalSet = new AnimatorSet();

        AnimatorSet enterAnimatorSet = getEnterAnimtor(imageView);//入场动画
        ValueAnimator bezierValueAnimator = getBezierValueAnimator(imageView);//贝塞尔曲线路径动画

        finalSet.playSequentially(enterAnimatorSet, bezierValueAnimator);
        finalSet.setInterpolator(new LinearInterpolator());
        finalSet.setTarget(imageView);

        finalSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                removeView((imageView));//删除爱心
                bezier.setVisibility(View.VISIBLE);
            }
        });
        finalSet.start();
    }

    /**
     * 获取中间的两个点
     */
    private PointF getPointF(int scale) {
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mHeight = getResources().getDisplayMetrics().heightPixels;


        PointF pointF = new PointF();
        pointF.x = random.nextInt((mWidth / 2));//减去50 是为了控制 x轴活动范围,看效果 随意~~
//        pointF.x = mWidth/2;//减去50 是为了控制 x轴活动范围,看效果 随意~~
        //再Y轴上 为了确保第二个点 在第一个点之上,我把Y分成了上下两半 这样动画效果好一些  也可以用其他方法
        pointF.y = random.nextInt((mHeight / 2 - 150)) / scale;
//        pointF.y = mHeight/2;
        return pointF;
    }


    private ValueAnimator getBezierValueAnimator(final View target) {
        //初始化一个BezierEvaluator
        BezierEvaluator evaluator = new BezierEvaluator(getPointF(2), getPointF(1));

        //第一个PointF传入的是初始点的位置
        ValueAnimator animator = ValueAnimator.ofObject(evaluator,
                new PointF((mWidth - dWidth) / 2, mHeight - dHeight - 20),
                new PointF(bezier.getX(), bezier.getY()));//随机
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                target.setX(pointF.x);
                target.setY(pointF.y);
                // alpha动画
//                target.setAlpha(1 - animation.getAnimatedFraction());
            }
        });
        animator.setTarget(target);
        animator.setDuration(3000);
        return animator;
    }


    private AnimatorSet getEnterAnimtor(final View target) {


        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 1.0f, 0.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1.0f, 0.3f);

        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        enter.setInterpolator(new LinearInterpolator());//线性变化
        enter.playTogether(scaleX, scaleY);
        enter.setTarget(target);

        return enter;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (musicController != null) {
            musicController.unregisterMediaControllerListener();
            musicController.unregisterMusicChangeListener();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (feetUiController != null) {
//            feetUiController.remove();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (musicController == null) {
//            Log.e(TAG, "onResume: ");
//            musicController = new MusicController(this);
//            if (downloadControler.getCurrentProgress().getDownloadPorgress() != 0) {
//                musicController.onPlay();
//            }
//            musicController.registerMusicChangeListener(new OnMusicChangeListener() {
//                @Override
//                public void onMusicChange(final Music info) {
//                    currentSongs = info;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            title.setText(info.getSongName());
//                            imageView.setImageBitmap(BitmapFactory.decodeFile(info.getImgPath()));
//
//                        }
//                    });
//                }
//            });
//
//
//            musicController.registerMediaControllerChangeListener(new OnMediaControllerListener() {
//                @Override
//                public void onMediaControllerChange() {
//                    MediaControllerCompat mediaControls = musicController.getMediaControls();
//                    if (mediaControls != null) {
//                        final MediaMetadataCompat metadata = mediaControls.getMetadata();
//                        if (metadata != null) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    title.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
//                                    imageView.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
//                                }
//                            });
//
//                        }
//                    }
//                }
//            });
//
//            musicController.registerStepChange(new IStepChange() {
//                @Override
//                public void getStepCount(double stepcount) {
//
//                }
//
//                @Override
//                public void getCurrentBpm(int bpm) {
//                    setStepFreq(bpm);
//                }
//            });
//
//        }

    }

    private void setStepFreq(int value) {
        if (currentSongs != null && musicController != null) {
            int tempo = Integer.parseInt(currentSongs.getTempo());
            if (value >= 120 && value <= 220) {
                bpm.setText(value + "");
                musicController.setTempo(value);
            } else {
                bpm.setText("120");
                musicController.setTempo(120);
            }
        }


    }


}
