package com.example;

public class HttpClientConfig {
    private final int poolSize;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final int writeTimeoutMillis;
    private final int requestTimeoutMillis;
    // Add other configuration parameters as needed, e.g., sslContext, proxy

    private HttpClientConfig(Builder builder) {
        this.poolSize = builder.poolSize;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.writeTimeoutMillis = builder.writeTimeoutMillis;
        this.requestTimeoutMillis = builder.requestTimeoutMillis;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public int getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public int getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int poolSize = 10; // Default pool size
        private int connectTimeoutMillis = 10000; // Default connect timeout of 10 seconds
        private int readTimeoutMillis = 15000; // Default read timeout
        private int writeTimeoutMillis = 10000; // Default write timeout
        private int requestTimeoutMillis = 20000; // Default total request timeout

        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder connectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder readTimeoutMillis(int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public Builder writeTimeoutMillis(int writeTimeoutMillis) {
            this.writeTimeoutMillis = writeTimeoutMillis;
            return this;
        }

        public Builder requestTimeoutMillis(int requestTimeoutMillis) {
            this.requestTimeoutMillis = requestTimeoutMillis;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }
}
