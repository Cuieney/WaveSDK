package com.feetsdk.android.feetsdk.musicplayer;

import android.graphics.Bitmap;

/**
 * Created by cuieney on 17/1/4.
 */
public class MusicInfo {
    private String songName;
    private String singerName;
    private Bitmap coverImg;


    public MusicInfo(String songName, String singerName, Bitmap coverImg) {
        this.songName = songName;
        this.singerName = singerName;
        this.coverImg = coverImg;
    }


    @Override
    public String toString() {
        return "MusicInfo{" +
                "coverImg=" + coverImg +
                ", singerName='" + singerName + '\'' +
                ", songName='" + songName + '\'' +
                '}';
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public Bitmap getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(Bitmap coverImg) {
        this.coverImg = coverImg;
    }
}
