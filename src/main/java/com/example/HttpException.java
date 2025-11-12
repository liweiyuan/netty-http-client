package com.example;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * HTTP异常类，用于处理各种HTTP相关的异常
 */
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public HttpException(int statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpException(HttpResponseStatus status, String responseBody) {
        this(status.code(), status.reasonPhrase(), responseBody);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}