package com.example;

import io.netty.handler.codec.http.HttpMethod;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final URI uri;
    private final byte[] body;
    private final Map<String, String> headers;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.body = builder.body;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
    }

    public HttpMethod getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpMethod method;
        private URI uri;
        private byte[] body;
        private Map<String, String> headers = new HashMap<>();

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder url(String url) {
            try {
                this.uri = new URI(url);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + url, e);
            }
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public HttpRequest build() {
            if (method == null) {
                throw new IllegalStateException("Method must be set");
            }
            if (uri == null) {
                throw new IllegalStateException("URI must be set");
            }
            return new HttpRequest(this);
        }
    }
}
