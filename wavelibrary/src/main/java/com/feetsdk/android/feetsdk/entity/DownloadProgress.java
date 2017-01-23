package com.feetsdk.android.feetsdk.entity;

/**
 * Created by cuieney on 16/11/16.
 */
public class DownloadProgress {
    private int downloadPorgress;
    private int downloadMinute;
    private int favoritePorgress;
    private int favoriteMinute;


    public DownloadProgress(int downloadPorgress, int downloadMinute, int favoritePorgress, int favoriteMinute) {
        this.downloadPorgress = downloadPorgress;
        this.downloadMinute = downloadMinute;
        this.favoritePorgress = favoritePorgress;
        this.favoriteMinute = favoriteMinute;
    }

    @Override
    public String toString() {
        return "DownloadProgress{" +
                "downloadPorgress=" + downloadPorgress +
                ", downloadMinute=" + downloadMinute +
                ", favoritePorgress=" + favoritePorgress +
                ", favoriteMinute=" + favoriteMinute +
                '}';
    }

    public int getDownloadPorgress() {
        return downloadPorgress;
    }

    public void setDownloadPorgress(int downloadPorgress) {
        this.downloadPorgress = downloadPorgress;
    }

    public int getDownloadMinute() {
        return downloadMinute;
    }

    public void setDownloadMinute(int downloadMinute) {
        this.downloadMinute = downloadMinute;
    }

    public int getFavoritePorgress() {
        return favoritePorgress;
    }

    public void setFavoritePorgress(int favoritePorgress) {
        this.favoritePorgress = favoritePorgress;
    }

    public int getFavoriteMinute() {
        return favoriteMinute;
    }

    public void setFavoriteMinute(int favoriteMinute) {
        this.favoriteMinute = favoriteMinute;
    }
}
