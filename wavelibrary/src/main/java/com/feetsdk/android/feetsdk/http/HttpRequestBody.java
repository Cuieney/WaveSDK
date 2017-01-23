package com.feetsdk.android.feetsdk.http;

import android.content.Context;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by cuieney on 16/11/10.
 */
class HttpRequestBody {
    private static HttpRequestBody singleton;

    public static HttpRequestBody with(Context context) {
        if (singleton == null) {
            synchronized (HttpRequestBody.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    public RequestBody provideJsonBody(String json){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        return body;
    }

    public static class Builder {
        Context context;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

        public HttpRequestBody build() {
            return new HttpRequestBody();
        }
    }
}
