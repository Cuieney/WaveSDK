package com.feetsdk.android.feetsdk.player;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.ISuperPowerPlayerService;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.stepcount.IStepChange;
import com.feetsdk.android.feetsdk.stepcount.StepDetector;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/11/21.
 */
public class MusicServiceHandler extends ISuperPowerPlayerService.Stub implements IMusicServiceHandler {
    private final WeakReference<MusicService> mService;
    public  StepDetector stepDetector;
    public  SensorManager sensormanager;
    private MusicMgr musicMgr;

    public MusicServiceHandler(WeakReference<MusicService> mService) {
        this.mService = mService;
        musicMgr = new MusicMgr(new WeakReference<Context>(mService.get()));

        sensormanager = (SensorManager) mService.get().getSystemService(Context.SENSOR_SERVICE);
        initPedometer();

        Logger.d("server MusicServiceHandler");
    }


    @Override
    public void onStartCommand(Intent intent, int flags, int startId) {
        if (musicMgr == null) {
            Logger.d("musicMgr");
            musicMgr = new MusicMgr(new WeakReference<Context>(mService.get()));
        }
        if (stepDetector == null) {
            Logger.d("stepDetector");
            initPedometer();
        }
        Logger.d("server onStartCommand");
    }

    private void initPedometer() {
        stepDetector = new StepDetector(sensormanager);
        stepDetector.registerStepListener(new IStepChange() {
            @Override
            public void getStepCount(double stepcount) {
                Intent intent1 = new Intent(PlayerControler.STEP_CHANGE_STEP);
                intent1.putExtra("stepCount",stepcount);
                mService.get().sendBroadcast(intent1);
            }

            @Override
            public void getCurrentBpm(int bpm) {
                Intent intent1 = new Intent(PlayerControler.STEP_CHANGE_BPM);
                intent1.putExtra("bpm",bpm);
                mService.get().sendBroadcast(intent1);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this;
    }

    @Override
    public void onDestroy() {

        Logger.d("server onDestroy");
    }

    @Override
    public void pause() throws RemoteException {
        musicMgr.pause();
    }

    @Override
    public void start() throws RemoteException {
        musicMgr.start();
    }

    @Override
    public void next() throws RemoteException {
        musicMgr.next();
    }

    @Override
    public void preview() throws RemoteException {
        musicMgr.preview();
    }

    @Override
    public void stop() throws RemoteException {
        if (musicMgr != null) {
            musicMgr.pause();
            musicMgr.stop();
            musicMgr = null;
        }
        if (stepDetector != null) {
            stepDetector.unregisterStepListener();
            stepDetector = null;
            sensormanager = null;
        }

        Logger.d("server stop");
    }

    @Override
    public void setTempo(float rate) throws RemoteException {
        musicMgr.setTempo(rate);
    }

    @Override
    public void setBpm(float bpm) throws RemoteException {
        musicMgr.setBpm(bpm);
    }

    @Override
    public void setSeek(int percent) throws RemoteException {
        musicMgr.setSeek(percent);
    }

    @Override
    public Music getCurrentMusic() throws RemoteException {
        return musicMgr.getCurrentMusic();
    }

    @Override
    public void play(int type) throws RemoteException {
        musicMgr.play(type);
    }

    @Override
    public boolean isPlaying() throws RemoteException {
        return musicMgr.isPlaying();
    }

    @Override
    public void favMusic() throws RemoteException {
        musicMgr.favMusic();
    }

}
