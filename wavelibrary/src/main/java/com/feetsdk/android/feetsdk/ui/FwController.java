package com.feetsdk.android.feetsdk.ui;

import android.app.Dialog;
import android.content.Context;

import com.feetsdk.android.common.rom.FloatPermission;

/**
 * Created by cuieney on 17/1/11.
 */
public class FwController {
    public  FloatPermission mFloatPermission;
    private Context context;
    public  FWProxy fwProxy;
    private String TAG = "FeetSDK";
    private static FwController controller;
    private Dialog dialog;

    private FwController() {
        mFloatPermission = new FloatPermission();
    }
    public synchronized static FwController getInstance(){
        if (controller == null) {
            controller = new FwController();
        }
        return controller;
    }

    private synchronized void checkProxyIsNull(Context context){
        if (fwProxy == null) {
            fwProxy = new FWProxy(context);
        }
    }
    public void show(Context context) {
        checkProxyIsNull(context);
        if (mFloatPermission == null) {
            mFloatPermission = new FloatPermission();
        }
        if (mFloatPermission.checkPermission(context)) {
            //调用浮动窗口
            fwProxy.show();
        }else{
            mFloatPermission.applyPermission();
        }
    }

    public void remove(Context context){
        checkProxyIsNull(context);
        fwProxy.remove();
        controller = null;
        context = null;
    }

    public void dismiss(Context context) {
        checkProxyIsNull(context);
        fwProxy.dismiss();
    }
}
