package com.feetsdk.android.feetsdk.player;

import android.content.Intent;
import android.os.IBinder;

/**
 * Created by cuieney on 16/11/21.
 */
public interface IMusicServiceHandler {
    void onStartCommand(Intent intent, int flags, int startId);

    IBinder onBind(Intent intent);

    void onDestroy();

}
