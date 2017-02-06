package com.phl.wavesdk;

import android.app.Application;
import android.content.Context;

import com.feetsdk.android.FeetSdk;
import com.feetsdk.android.common.utils.Logger;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by cuieney on 17/1/9.
 *
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FeetSdk.getInstance(this).init("a16f508e-3c97-4f7a-85d4-b2ef33508157","demo");
        FeetSdk.getInstance(this).setMobileNetWorkVisiable(true);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private DaemonClient mDaemonClient;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mDaemonClient = new DaemonClient(createDaemonConfigurations());
        mDaemonClient.onAttachBaseContext(base);
    }



    private DaemonConfigurations createDaemonConfigurations(){
        Logger.e("DaemonConfigurations");
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                "com.marswin89.marsdaemon.demo:process1",
                Service1.class.getCanonicalName(),
                Receiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                "com.marswin89.marsdaemon.demo:process2",
                Service2.class.getCanonicalName(),
                Receiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }


    class MyDaemonListener implements DaemonConfigurations.DaemonListener{
        @Override
        public void onPersistentStart(Context context) {
            Logger.e("onPersistentStart");
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
            Logger.e("onDaemonAssistantStart");
        }

        @Override
        public void onWatchDaemonDaed() {
            Logger.e("onWatchDaemonDaed");
        }
    }

}
