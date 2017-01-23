package com.feetsdk.android.feetsdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cuieney on 16/11/21.
 */
public class Music implements Parcelable{
    private String songId;

    private String songName;

    private String coverImageUrl;

    private int progress;


    private String path;

    private String singerName;

    private String tempo;

    private int size;

    private boolean collection;

    private boolean listener;

    private String imgPath;


    public Music(String songId, String songName, String coverImageUrl, int progress, String path, String singerName, String tempo, int size, boolean collection, boolean listener,String imgPath) {
        this.songId = songId;
        this.songName = songName;
        this.coverImageUrl = coverImageUrl;
        this.progress = progress;
        this.path = path;
        this.singerName = singerName;
        this.tempo = tempo;
        this.size = size;
        this.collection = collection;
        this.listener = listener;
        this.imgPath = imgPath;
    }

    public Music() {
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public boolean isListener() {
        return listener;
    }

    public void setListener(boolean listener) {
        this.listener = listener;
    }


    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    @Override
    public String toString() {
        return "Music{" +
                "songId='" + songId + '\'' +
                ", songName='" + songName + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", progress=" + progress +
                ", path='" + path + '\'' +
                ", singerName='" + singerName + '\'' +
                ", tempo='" + tempo + '\'' +
                ", size=" + size +
                ", collection=" + collection +
                ", listener=" + listener +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songId);
        dest.writeString(songName);
        dest.writeString(coverImageUrl);
        dest.writeInt(progress);
        dest.writeString(path);
        dest.writeString(singerName);
        dest.writeString(tempo);
        dest.writeInt(size);
        dest.writeByte((byte) (collection?1:0));
        dest.writeByte((byte) (listener?1:0));
        dest.writeString(imgPath);
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public Music(Parcel in){
        songId = in.readString();
        songName = in.readString();
        coverImageUrl = in.readString();
        progress = in.readInt();
        path = in.readString();
        singerName = in.readString();
        tempo = in.readString();
        size = in.readInt();
        collection = in.readByte() != 0;
        listener = in.readByte() != 0;
        imgPath = in.readString();
    }
}
