package com.feetsdk.android.feetsdk.musicplayer;

/**
 * Created by cuieney on 16/8/25.
 */
public interface ISuperPowerPlayer {
    void onPlayPause(boolean play);
    void play(String url, boolean isRemix);
    void setTempo(float rate);
    void setBpm(float bpm);
    void setSeek(int percent);
    boolean isPlaying();
    long getDurationSeconds();
    long getPositonSeconds();
}
