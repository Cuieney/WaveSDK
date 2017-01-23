package com.feetsdk.android.feetsdk.entity;

/**
 * Created by cuieney on 16/11/30.
 */
public class UpdateProgress {
    private int precent;
    private int minute;

    @Override
    public String toString() {
        return "UpdateProgress{" +
                "precent=" + precent +
                ", minute=" + minute +
                '}';
    }

    public UpdateProgress(int precent, int minute) {
        this.precent = precent;
        this.minute = minute;
    }

    public int getPrecent() {
        return precent;
    }

    public void setPrecent(int precent) {
        this.precent = precent;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
