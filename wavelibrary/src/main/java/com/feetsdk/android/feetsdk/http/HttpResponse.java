package com.feetsdk.android.feetsdk.http;

/**
 * Created by cuieney on 16/11/10.
 */
public class HttpResponse {
    private int code;
    private String message;

    public HttpResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
