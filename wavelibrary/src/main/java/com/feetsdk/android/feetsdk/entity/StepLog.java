package com.feetsdk.android.feetsdk.entity;

import java.util.List;

/**
 * Created by cuieney on 16/12/12.
 */
public class StepLog {

    /**
     * user : a8ccdb12-ffa1-4af4-a9e4-9c2e3ddf72d9
     * step : 89
     * userBpm : 178
     * axisnum : 0
     * device : notget
     * indatas : ["1.005174092482775 6.5505933272652328"]
     */

    private String user;
    private int step;
    private int userBpm;
    private int axisnum;
    private String device;
    private List<List<Double>> indatas;

    public StepLog(String device, List<List<Double>> indatas, int step, int bpm, int size) {
        this.device = device;
        this.indatas = indatas;
        this.user = device;
        this.step = step;
        this.userBpm = bpm;
        this.axisnum = size;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getUserBpm() {
        return userBpm;
    }

    public void setUserBpm(int userBpm) {
        this.userBpm = userBpm;
    }

    public int getAxisnum() {
        return axisnum;
    }

    public void setAxisnum(int axisnum) {
        this.axisnum = axisnum;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public List<List<Double>> getIndatas() {
        return indatas;
    }

    public void setIndatas(List<List<Double>> indatas) {
        this.indatas = indatas;
    }
}
