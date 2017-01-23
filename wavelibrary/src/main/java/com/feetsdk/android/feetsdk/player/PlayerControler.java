package com.feetsdk.android.feetsdk.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.feetsdk.android.feetsdk.ISuperPowerPlayerService;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.player.callBack.IMusicChange;
import com.feetsdk.android.feetsdk.stepcount.IStepChange;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/11/21.
 */
public class PlayerControler implements IMusicControler{
    public static final String MUSIC_CHANGE = "MUSIC_CHANGE";
    public static final String STEP_CHANGE_BPM = "STEP_CHANGE_BPM";
    public static final String STEP_CHANGE_STEP = "STEP_CHANGE_STEP";

    private IMusicChange change;
    private IStepChange stepChange;

    private WeakReference<Context> context;
    private ISuperPowerPlayerService iSuperPowerPlayerService;
    private int type;

    private BroadcastReceiver playerServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastReceived(context, intent);
        }
    };
    public final ContextWrapper contextWrapper;

    private void handleBroadcastReceived(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MUSIC_CHANGE:
                if (change != null) {
                    if (getCurrentMusic() != null) {
                        change.currentMusic(getCurrentMusic());
                    }
                }
                break;
            case STEP_CHANGE_BPM:
                int bpm = intent.getIntExtra("bpm", 120);
                if (stepChange != null) {
                    stepChange.getCurrentBpm(bpm);
                }
                break;
            case STEP_CHANGE_STEP:
                double stepCount = intent.getDoubleExtra("stepCount",0.0);
                if (stepChange != null) {
                    stepChange.getStepCount(stepCount);
                }
                break;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iSuperPowerPlayerService = ISuperPowerPlayerService.Stub.asInterface(service);
            play(type);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iSuperPowerPlayerService = null;

        }
    };




    public PlayerControler(Context context,int type) {
        this.context = new WeakReference<>(context);
        this.type = type;


        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicService.class), mConnection, 0)) {
        }



        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);

        context.bindService(intent,mConnection,Context.BIND_AUTO_CREATE);


        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_CHANGE);
        filter.addAction(STEP_CHANGE_BPM);
        filter.addAction(STEP_CHANGE_STEP);
        context.registerReceiver(playerServiceBroadcastReceiver, filter);
    }

    @Override
    public void pause() {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.pause();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.start();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void next() {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.next();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preview() {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.preview();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        contextWrapper.unbindService(mConnection);

        if (iSuperPowerPlayerService != null) {
            contextWrapper.stopService(new Intent(context.get(),
                    MusicService.class));
        }

        context.get().unregisterReceiver(playerServiceBroadcastReceiver);

        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.stop();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setTempo(float rate) {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.setTempo(rate);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBpm(float bpm) {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.setBpm(bpm);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSeek(int percent) {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.setSeek(percent);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Music getCurrentMusic() {
        Music music = new Music();
        try {
            if (iSuperPowerPlayerService != null) {
                music = iSuperPowerPlayerService.getCurrentMusic();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return music;
    }

    @Override
    public void play(int type) {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.play(type);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlaying()  {
        boolean playing = false;
        try {
            if (iSuperPowerPlayerService != null) {
                playing = iSuperPowerPlayerService.isPlaying();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return playing;
    }

    @Override
    public void favMusic() {
        try {
            if (iSuperPowerPlayerService != null) {
                iSuperPowerPlayerService.favMusic();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void registerMusicChange(IMusicChange change){
        this.change = change;
    }

    public void registerStepChange(IStepChange change){
        this.stepChange = change;
    }
}
