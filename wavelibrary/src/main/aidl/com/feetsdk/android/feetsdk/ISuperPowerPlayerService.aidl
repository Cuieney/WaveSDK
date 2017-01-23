// ISuperPowerPlayerService.aidl
package com.feetsdk.android.feetsdk;

// Declare any non-default types here with import statements
import com.feetsdk.android.feetsdk.Music;
interface ISuperPowerPlayerService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void pause();
     void start();
     void next();
     void preview();
     void stop();
     void setTempo(float rate);
     void setBpm(float bpm);
     void setSeek(int percent);
     Music getCurrentMusic();
     void play(int type);
     boolean isPlaying();
     void favMusic();
}
