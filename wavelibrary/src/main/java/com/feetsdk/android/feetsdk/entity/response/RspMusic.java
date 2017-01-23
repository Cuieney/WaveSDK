package com.feetsdk.android.feetsdk.entity.response;

/**
 * Created by cuieney on 16/11/11.
 */
public class RspMusic {

    private String artistId;
    private String coverImageUrl;
    private String mp3Url;
    private String songId;
    private double duration;
    private double tempo;
    private String artistName;
    private String name;
    private boolean isFavorited;

    @Override
    public String toString() {
        return "RspMusic{" +
                "artistId='" + artistId + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", mp3Url='" + mp3Url + '\'' +
                ", songId='" + songId + '\'' +
                ", duration=" + duration +
                ", tempo=" + tempo +
                ", artistName='" + artistName + '\'' +
                ", name='" + name + '\'' +
                ", isFavorited=" + isFavorited +
                '}';
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
