package com.example;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class RichHttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final byte[] body;

    private RichHttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers;
        this.body = body;
    }

    public static RichHttpResponse from(FullHttpResponse response) {
        HttpResponseStatus status = response.status();
        HttpHeaders h = response.headers();
        Map<String, String> copiedHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : h) {
            copiedHeaders.put(e.getKey(), e.getValue());
        }
        byte[] bodyBytes = new byte[response.content().readableBytes()];
        response.content().getBytes(response.content().readerIndex(), bodyBytes);
        return new RichHttpResponse(status.code(), status.reasonPhrase(), copiedHeaders, bodyBytes);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBodyBytes() {
        return body;
    }

    public String getBody() {
        return new String(body, CharsetUtil.UTF_8);
    }
}
