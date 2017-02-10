package com.feetsdk.android.feetsdk.stepcount;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by cuieney on 16/12/21.
 */
public class StepDetector implements SensorEventListener {
    final private SensorManager mSensorMgr;
    IStepChange change;

    private ScheduledExecutorService mGeneratorExecutor;

    /**
     * 1s计时器（用于计算当前步频和步数）
     */
    private ScheduledFuture<?> interval;

    /**
     * 保存每秒钟传感器变化的值
     */
    private List<Double> sensorList;

    /**
     * 计步器算法逻辑
     */
    public PedometerLogic logic;

    private double[] datas;
    public StepDetector(SensorManager mSensorMgr) {
        this.mSensorMgr = mSensorMgr;
        mGeneratorExecutor = new ScheduledThreadPoolExecutor(2);
        sensorList = new ArrayList<>();
        datas = new double[2];
    }


    public void registerStepListener(@NonNull IStepChange change) {
        this.change = change;
        mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        interval = mGeneratorExecutor.scheduleWithFixedDelay(new CalSensorData(),1,1, TimeUnit.SECONDS);
        logic = new PedometerLogic();
    }

    public void unregisterStepListener() {
        mSensorMgr.unregisterListener(this);
        interval.cancel(true /* mayInterruptIfRunning */);
        logic = null;
        change = null;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        /*
      记录当前传感器变化的轴平方根
     */
        double currentData = values[0] * values[0] + values[1] * values[1] + values[2] * values[2];
        sensorList.add(currentData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private double[] getData(){
        return datas;
    }

    private class CalSensorData implements Runnable{
        @Override
        public void run() {
            double[] controller = logic.controller(sensorList);
            datas = controller;
            if (change != null) {
                change.getCurrentBpm(((int) controller[1]));
                change.getStepCount(controller[0]);
                sensorList.clear();
            }

        }
    }
}
