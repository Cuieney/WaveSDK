package com.feetsdk.android.feetsdk.stepcount;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.feetsdk.android.common.utils.Logger;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/12/21.
 */
 class Pedometer implements IController{
    private StepService mService;
    private WeakReference<Context> ctx;
    private IStepChange change;

    public Pedometer(Context context) {
        this.ctx = new WeakReference<>(context);

        startStepService(ctx.get());
        bindStepService(ctx.get());
        Logger.d("pedometer create");
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((StepService.StepBinder) service).getService();
            mService.reigisterStepListener(new IStepChange() {
                @Override
                public void getStepCount(double stepcount) {
                    if (change != null) {
                        change.getStepCount(stepcount);
                    }
                }

                @Override
                public void getCurrentBpm(int bpm) {
                    if (change != null) {
                        change.getCurrentBpm(bpm);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private void startStepService(Context context) {
        context.startService(new Intent(context,
                StepService.class));
    }

    private void bindStepService(Context context) {
        context.bindService(new Intent(context, StepService.class), connection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    public void destroyService() {
        StepController.healthControl = null;
        if (ctx != null) {
            unbindStepService(ctx.get());
            stopStepService(ctx.get());
        }
        ctx = null;
        Logger.d("step service destroy");
    }


    private void stopStepService(Context mContext) {
        if (mService != null) {
            mContext.stopService(new Intent(mContext,
                    StepService.class));
        }
    }

    public void getStepChange(IStepChange change){
        this.change = change;
    }


    private void unbindStepService(Context context) {
        context.unbindService(connection);
    }
}
