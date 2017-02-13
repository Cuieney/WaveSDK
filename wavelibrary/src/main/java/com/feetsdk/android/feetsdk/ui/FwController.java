package com.feetsdk.android.feetsdk.ui;

import android.content.Context;

import com.feetsdk.android.common.rom.FloatPermission;

/**
 * Created by cuieney on 17/1/11.
 */
public class FwController {
    public FloatPermission mFloatPermission;
    public FWProxy fwProxy;
    private static FwController controller;
    private int location;

    private FwController() {
        mFloatPermission = new FloatPermission();
    }

    public synchronized static FwController getInstance() {
        if (controller == null) {
            controller = new FwController();
        }
        return controller;
    }

    private synchronized void checkProxyIsNull(Context context) {
        if (fwProxy == null) {
            fwProxy = new FWProxy(context, location);
        }
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void show(Context context) {
        checkProxyIsNull(context);
        if (mFloatPermission == null) {
            mFloatPermission = new FloatPermission();
        }
        if (mFloatPermission.checkPermission(context)) {
            //调用浮动窗口
            fwProxy.show();
        } else {
            mFloatPermission.applyPermission();
        }
    }

    public void remove() {
        fwProxy.remove();
        controller = null;
    }

    public void dismiss() {
        fwProxy.dismiss();
    }

    public void pauseMusic() {
        fwProxy.pauseMusic();
    }

    public void stopMusic() {
        fwProxy.showDownload();
    }

    public void playMusic() {
        fwProxy.playMusic();
    }

    public void setBpm(int tempo){
        fwProxy.setTempo(tempo);
    }

    public void setAutoBpm(boolean isAuto){
        fwProxy.setAutoTempo(isAuto);
    }
}
