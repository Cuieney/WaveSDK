package com.phl.wavesdk;

import android.app.Application;

import com.feetsdk.android.FeetSdk;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by cuieney on 17/1/9.
 *
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FeetSdk.getInstance().init(this,"99b36eda-3c91-4715-84ee-480c90ffe82f","demo");
        FeetSdk.getInstance().setMobileNetWorkAvailable(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }


}
