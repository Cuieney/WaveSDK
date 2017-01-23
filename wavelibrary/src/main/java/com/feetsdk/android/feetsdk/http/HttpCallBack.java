package com.feetsdk.android.feetsdk.http;

import com.feetsdk.android.common.exception.HttpException;

/**
 * Created by cuieney on 16/11/10.
 */
interface HttpCallBack {
    void response(HttpResponse response);

    void failed(HttpException exception);
}
