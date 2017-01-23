package com.feetsdk.android.feetsdk.player;

import com.feetsdk.android.feetsdk.Music;

/**
 * Created by cuieney on 16/11/21.
 */
public interface IMusicControler {

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
