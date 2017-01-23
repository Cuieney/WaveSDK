package com.feetsdk.android.feetsdk.musicplayer;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * Created by cuieney on 16/10/17.
 */
public class MusicProxy {
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PLAY_SONG = "ACTION_PLAY_SONG";
    public static final String ACTION_PREV_SONG = "ACTION_PREV_SONG";
    public static final String ACTION_PAUSE_SONG = "ACTION_PAUSE_SONG";
    public static final String ACTION_CONTINUE_SONG = "ACTION_CONTINUE_SONG";
    public static final String ACTION_STOP_MUSIC = "ACTION_STOP_MUSIC";
    public static final String ACTION_MEDIA_SESSION = "ACTION_MEDIA_SESSION";
    public static final String ACTION_MEDIA_DATA = "ACTION_MEDIA_DATA";
    public static final String ACTION_SET_TEMPO = "ACTION_SET_TEMPO";
    public static final String ACTION_SET_SEEK = "ACTION_SET_SEEK";
    public static final String STEP_CHANGE_BPM = "STEP_CHANGE_BPM";
    public static final String STEP_CHANGE_STEP = "STEP_CHANGE_STEP";
    public static final String ACTION_PLAYBACK_STATE = "ACTION_PLAYBACK_STATE";
    public static final String CUSTOME_ACTION_UPDATED_SONG = "CUSTOME_ACTION_UPDATED_SONG";


    public static final String KEY_TEMPO = "KEY_TEMPO";
    public static final String KEY_SEEK = "KEY_SEEK";
    public static final String KEY_TOKEN= "KEY_TOKEN";
    public static final String KEY_MEDIA_DATA= "KEY_MEDIA_DATA";
    public static final String KEY_PLAYBACK_STATE= "KEY_PLAYBACK_STATE";
    public static final String KEY_CUSTOM_ACTION= "KEY_CUSTOM_ACTION";

    private WeakReference<Context> wct;

    public MusicProxy(Context wct) {
        this.wct = new WeakReference<>(wct);
    }

    private Context getCtx(){
        if (wct.get() != null) {
            return wct.get();
        }
        return null;
    }

    public  void init(){
        if (getCtx() != null) {
            getCtx().startService(new Intent(getCtx(),MusicService.class));
//            getCtx().startService(new Intent(getCtx(),PlayNannyService.class));
        }
    }

    public void customeAction(String action, Bundle extras){
        if (getCtx() != null) {
            Intent intent = new Intent(CUSTOME_ACTION_UPDATED_SONG);
            intent.putExtra(KEY_CUSTOM_ACTION,action);
            intent.putExtras(extras);
            getCtx().sendBroadcast(intent);
        }
    }

    public  void playerMusic() {
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_PLAY_SONG));
        }
    }

    public  void nextMusic() {
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_NEXT_SONG));
        }
    }

    public  void prevMusic() {
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_PREV_SONG));
        }
    }

    public  void pauseMusic() {
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_PAUSE_SONG));
        }
    }

    public  void continueMusic(){
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_CONTINUE_SONG));
        }
    }

    public  void stopMusic(){
        if (getCtx() != null) {
            getCtx().sendBroadcast(new Intent(ACTION_STOP_MUSIC));
        }
    }

    public  void setTempo(float tempo) {
        if (getCtx() != null) {
            Intent intent = new Intent(ACTION_SET_TEMPO);
            intent.putExtra(KEY_TEMPO,tempo);
            getCtx().sendBroadcast(intent);
        }
    }

    public  void setSeek(int percent) {
        if (getCtx() != null) {
            Intent intent = new Intent(ACTION_SET_SEEK);
            intent.putExtra(KEY_SEEK,percent);
            getCtx().sendBroadcast(intent);
        }
    }


}
