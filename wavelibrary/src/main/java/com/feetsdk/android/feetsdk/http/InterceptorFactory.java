package com.feetsdk.android.feetsdk.http;

import android.content.Context;

import com.feetsdk.android.FeetConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


class InterceptorFactory {

    private static final int TIMEOUT_CONNECT = 5; //5秒
    private static final int TIMEOUT_DISCONNECT = 60 * 60 * 24 * 10; //7天
    Context context;

    public InterceptorFactory(Context context) {
        this.context = context;
    }

    public  Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            String cacheControl = originalResponse.header("Cache-Control");
            //如果cacheControl为空，就让他TIMEOUT_CONNECT秒的缓存，本例是5秒，方便观察
            if (cacheControl == null) {
                originalResponse = originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + TIMEOUT_CONNECT)
                        .build();
                return originalResponse;
            } else {
                return originalResponse;
            }
        }
    };

    public  Interceptor ADD_HEAD_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + new FeetConfig(context).getAppkey())
                    .build();
            return chain.proceed(request);
        }
    };
}