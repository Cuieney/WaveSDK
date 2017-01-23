package com.feetsdk.android;

import android.content.Context;

/**
 * Created by cuieney on 16/11/9.
 */
interface IFeetConfig {
    /**
     * 初始化设备信息
     * @param context 上下文
     */
    void initDevInfo(Context context);

    /**
     * 获取appkey
     * @return
     */
    String getAppkey();

    /**
     * 获取渠道
     * @return
     */
    String getAppChannel();

    /**
     * 获取剩余时间
     * @return
     */
    int getRestTime();

    /**
     * 判断免费使用时间是否结束
     * @return
     */
    boolean isTimeOut();

    /**
     * 设置appkey
     * @param appKey
     */
    void setAppKey(String appKey);

    /**
     * 设置app渠道
     * @param appChannel
     */
    void setAppChannel(String appChannel);

    /**
     * 判断是否初始化过
     * @param appKey
     * @param appChannel
     * @return
     */
    boolean checkSelf(String appKey,String appChannel);

    /**
     * 初始化app信息
     * @param context
     */
    void initAppInfo(Context context);

    /**
     * 设置蜂窝数据
     * @param isOpen
     */
    void setCellularData(boolean isOpen);


    void debug(String debug);

    /**
     * 判断是否打开蜂窝数据进行下载
     * @return
     */
    boolean getCellularData();


}
