package com.feetsdk.android.feetsdk.http;

import com.feetsdk.android.common.exception.HttpException;

/**
 * Created by cuieney on 16/11/14.
 */
public interface IHttpRspCallBack {
    void success(HttpResponse response);
    void failed(HttpException exception);
}
