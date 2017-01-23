package com.feetsdk.android.feetsdk.download;

import com.liulishuo.filedownloader.BaseDownloadTask;

/**
 * Created by cuieney on 16/9/9.
 */
public class ProgressBus {
    BaseDownloadTask task;
    long soFarBytes;
    long totalByte;

    public ProgressBus(BaseDownloadTask task, int soFarBytes, int totalByte) {
        this.task = task;
        this.soFarBytes = soFarBytes;
        this.totalByte = totalByte;
    }

    @Override
    public String toString() {
        return "ProgressBus{" +
                "task=" + task +
                ", soFarBytes=" + soFarBytes +
                ", totalByte=" + totalByte +
                '}';
    }

    public BaseDownloadTask getTask() {
        return task;
    }

    public void setTask(BaseDownloadTask task) {
        this.task = task;
    }

    public long getSoFarBytes() {
        return soFarBytes;
    }

    public void setSoFarBytes(long soFarBytes) {
        this.soFarBytes = soFarBytes;
    }

    public long getTotalByte() {
        return totalByte;
    }

    public void setTotalByte(long totalByte) {
        this.totalByte = totalByte;
    }
}
