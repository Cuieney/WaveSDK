package com.feetsdk.android.feetsdk.download;

import android.content.Context;

import com.feetsdk.android.feetsdk.annotation.RequestStartDownload;
import com.feetsdk.android.feetsdk.entity.DownloadProgress;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/11/14.
 */
public class DownloadControler  {

    private WeakReference<Context> contextWeakReference;
    private DownloadProxy proxy;

    public DownloadControler(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private static DownloadControler downloadControler;

    private Context getContext(){
        return contextWeakReference.get();
    }
    public static DownloadControler getInstance(Context context){
        synchronized (DownloadControler.class){
            if (downloadControler == null) {
                downloadControler = new DownloadControler(context);
            }
        }
        return downloadControler;
    }


    public void startDownload(IUpdateProgressCallBack callBack,@RequestStartDownload int type) {
        if (proxy == null) {
            proxy = new DownloadProxy(getContext(),callBack);
        }else {
            proxy.setCallback(callBack);
        }
        proxy.startDownload(type);
    }



    public void pauseDownload() {
        if (proxy != null) {
            proxy.pauseDownload();
        }
    }

    public synchronized DownloadProgress getCurrentProgress(){
        if (proxy == null) {
            proxy = new DownloadProxy(getContext());
        }
        return proxy.getProgress();
    }

    public void restart(@RequestStartDownload int type){
        proxy.restart(type);
    }

    public void resetPause(){
        proxy.resetPause();
    }

    public void delete(){
        proxy.delete();
    }

}
