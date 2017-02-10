package com.feetsdk.android.feetsdk.stepcount;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/12/21.
 */
 class PedometerLogic {
    private String TAG = "PedometerLogic";

    private static final double Q = 0.001;
    private static final double R = 0.01;


    public static List<List<Double>> arrayAll;


    private static int saveWindow = 30;

    static {
        arrayAll = new ArrayList<>();
        for (int i = 0; i < saveWindow; i++) {
            arrayAll.add(i, new ArrayList<Double>());
        }
    }


    //卡尔曼优化算法
    private double[] calmanFilter(double[] datas) {
        double[] x_measure = normalization(datas);
        double[] x_optimum = new double[x_measure.length];
        x_optimum[0] = 0;
        double p_cal = 0.1;
        double k_k;

        for (int i = 1; i < x_optimum.length; i++) {
            k_k = (p_cal + Q) / (p_cal + Q + R);
            x_optimum[i] = x_optimum[i - 1] + k_k * (x_measure[i] - x_optimum[i - 1]);
            p_cal = (1 - k_k) * (p_cal + Q);

        }

        return x_optimum;
    }


    //波峰个数
    private int countPeak(double[] array, int window) {
        int count = 0;
        for (int i = window; i <= array.length - window - 1; i++) {
            int num = 0;
            for (int j = 0; j <= window; j++) {
                if (array[i] > array[i + j] && array[i] > array[i - j] && array[i] > sum(array) / array.length) {
                    num++;
                }
            }
            if (num == (window)) {
                count++;
            }
        }
//        Log.i(TAG, "countPeak: "+count);

        return count;

    }


    //bpm换算
    private double[] bpmCount(double timeDuration) {
        List<Double> indatas = new ArrayList<>();

        for (int i = (saveWindow - ((int) timeDuration)); i < saveWindow; i++) {
            List<Double> doubles = arrayAll.get(i);
            for (int j = 0; j < doubles.size(); j++) {
                indatas.add(doubles.get(j));
            }
        }
        double[] array = new double[indatas.size()];
        for (int i = 0; i < indatas.size(); i++) {
            array[i] = indatas.get(i);
        }

        double step = 0.0;
        double[] x_optimun = calmanFilter(array);
//        Log.i(TAG, "variance: " + variance(middle(array)));
        if (variance(middle(array)) > 0.15) {
            int window = 4;
            step = countPeak(x_optimun, window);
        }

        double userBpm = step * 60 / timeDuration;
        return new double[]{step / timeDuration, userBpm};
    }

    //bpm稳定机制
    private double[] bpmStabilie(double[] bpm1, double[] bpm2) {
        double[] bpmNow = bpm2;
        String aaa = "bpm2";

        //bpm1[1] - bpm2[1] > 20
        List<Double> firstValue = arrayAll.get(0);
        if (firstValue.size() == 0) {
            bpmNow = bpm1;
            aaa = "bpm1";
        }
//        Log.i(TAG, "aaa: " + aaa);

        if (bpmNow[1] <= 120.0) {
            bpmNow[1] = 120;
        } else if (bpm1[1] >= 200) {
            bpmNow[1] = 200;
        }
        return bpmNow;
    }


    private void arrayAdd(double[] array) {
        List<Double> mid = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            mid.add(array[i]);
        }

        List<List<Double>> moveList = new ArrayList<>();

        for (int i = 0; i < saveWindow; i++) {
            if (i != saveWindow - 1) {
                moveList.add(arrayAll.get(i + 1));
            } else {
                moveList.add(mid);
            }
        }

        arrayAll = moveList;

//        Log.i(TAG, "arrayAdd: "+arrayAll.get(arrayAll.size()-1));
    }

    public double[] controller(List<Double> log) {
        double[] array = new double[log.size()];
        for (int i = 0; i < log.size(); i++) {
            array[i] = log.get(i);
        }
        arrayAdd(array);


        double[] bpm1 = new double[2];
        double[] bpm2 = new double[2];

        List<Double> firstValue = arrayAll.get(0);
//        Log.i(TAG, "controller: "+firstValue.size()+",,,"+log.size());
        if (firstValue.size() == 0) {
            double timeDuration1 = 8.0;
            bpm1 = bpmCount(timeDuration1);
            bpm2 = bpm1;

        } else {

            double timeDuration2 = 30.0;
            bpm2 = bpmCount(timeDuration2);
            bpm1 = bpm2;
        }

        double[] outDatas = bpmStabilie(bpm1, bpm2);
        return outDatas;
    }


    private double variance(double[] array) {
        double num = 0;
        double num2 = 0;
        for (int i = 0; i < array.length; i++) {
            num += array[i];
            num2 += array[i] * array[i];
        }
        double value = (num2 / array.length) - (num / array.length) * (num / array.length);
        return Math.sqrt(value);
    }


    private double sum(double[] array) {
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            count += array[i];

        }
        return count;
    }

    //标准化
    private double[] normalization(double[] datas) {
        double mean = sum(datas) / datas.length;
        double standard = variance(datas);
        double[] out = new double[datas.length];
        for (int i = 0; i < datas.length; i++) {
            out[i] = (datas[i] - mean) / standard;
        }

        return out;
    }


    //中性化
    private double[] middle(double[] datas) {
        double max = 0;
        double min = 0;
        for (int i = 0; i < datas.length; i++) {
            if (datas[i] > max) {
                max = datas[i];
            }
            if (min > datas[i]) {
                min = datas[i];
            }

        }

        double[] out = new double[datas.length];
        for (int i = 0; i < datas.length; i++) {
            out[i] = (datas[i] - min) / (max - min);
        }

        return out;
    }
}
