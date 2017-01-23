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

    private FwController(Context context) {
        this.context = context;
        fwProxy = new FWProxy(context);
        mFloatPermission = new FloatPermission(context);
    }
    public synchronized static FwController getInstance(Context context){
        if (controller == null) {
            controller = new FwController(context);
        }
        return controller;
    }

    public void show() {
        if (mFloatPermission == null) {
            mFloatPermission = new FloatPermission(context);
        }
        if (mFloatPermission.checkPermission()) {
            //调用浮动窗口
            fwProxy.show();
        }else{
            mFloatPermission.applyPermission();
        }
    }

    public void remove(){
        fwProxy.remove();
        controller = null;
        context = null;
    }

    public void dismiss() {
        fwProxy.dismiss();
    }
}
