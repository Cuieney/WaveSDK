package com.feetsdk.android.common.exception;

/**
 * Created by cuieney on 16/11/9.
 */
public class HttpException extends BaseException {
    private static final long serialVersionUID = 1L;
    private int exceptionCode;

    public HttpException() {
    }

    public HttpException(String detailMessage) {
        super(detailMessage);
    }

    public HttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public HttpException(Throwable throwable) {
        super(throwable);
    }

    public HttpException(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public HttpException(int exceptionCode, String detailMessage) {
        super(detailMessage);
        this.exceptionCode = exceptionCode;
    }

    public HttpException(int exceptionCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.exceptionCode = exceptionCode;
    }

    public HttpException(int exceptionCode, Throwable throwable) {
        super(throwable);
        this.exceptionCode = exceptionCode;
    }

    public int getExceptionCode() {
        return this.exceptionCode;
    }
}
