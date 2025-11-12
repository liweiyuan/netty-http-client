package com.example;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.HttpRequest;
import com.example.HttpClientConfig;
import com.example.NettyHttpClientCore;

public class NettyHttpClient {
    private final ConnectionPool connectionPool;
    private final HttpClient httpClient;
    private final HttpClientConfig config;

    private NettyHttpClient(Builder builder) {
        this.config = builder.config;
        this.httpClient = builder.httpClient != null ? builder.httpClient : new NettyHttpClientCore();
        this.httpClient.getBootstrap().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis());
this.connectionPool = new ConnectionPool(httpClient.getBootstrap(), config);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpClientConfig config = HttpClientConfig.builder().build(); // Default config
        private HttpClient httpClient;

        public Builder config(HttpClientConfig config) {
            this.config = config;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public NettyHttpClient build() {
            return new NettyHttpClient(this);
        }
    }

    public CompletableFuture<RichHttpResponse> get(String url) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.GET).url(url).build());
    }

    public CompletableFuture<RichHttpResponse> get(String url, Map<String, String> headers) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.GET).url(url).headers(headers).build());
    }

    public CompletableFuture<RichHttpResponse> post(String url, byte[] body) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.POST).url(url).body(body).build());
    }

    public CompletableFuture<RichHttpResponse> post(String url, byte[] body, Map<String, String> headers) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.POST).url(url).body(body).headers(headers).build());
    }

    public CompletableFuture<RichHttpResponse> put(String url, byte[] body) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.PUT).url(url).body(body).build());
    }

    public CompletableFuture<RichHttpResponse> put(String url, byte[] body, Map<String, String> headers) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.PUT).url(url).body(body).headers(headers).build());
    }

    public CompletableFuture<RichHttpResponse> delete(String url) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.DELETE).url(url).build());
    }

    public CompletableFuture<RichHttpResponse> delete(String url, Map<String, String> headers) {
        return sendRequest(HttpRequest.builder().method(HttpMethod.DELETE).url(url).headers(headers).build());
    }

    public RichHttpResponse getSync(String url) {
        return get(url).join();
    }

    public RichHttpResponse getSync(String url, Map<String, String> headers) {
        return get(url, headers).join();
    }

    public RichHttpResponse postSync(String url, byte[] body) {
        return post(url, body).join();
    }

    public RichHttpResponse postSync(String url, byte[] body, Map<String, String> headers) {
        return post(url, body, headers).join();
    }

    public RichHttpResponse putSync(String url, byte[] body) {
        return put(url, body).join();
    }

    public RichHttpResponse putSync(String url, byte[] body, Map<String, String> headers) {
        return put(url, body, headers).join();
    }

    public RichHttpResponse deleteSync(String url) {
        return delete(url).join();
    }

    public RichHttpResponse deleteSync(String url, Map<String, String> headers) {
        return delete(url, headers).join();
    }

    private CompletableFuture<RichHttpResponse> sendRequest(HttpRequest request) {
        URI uri = request.getUri();
        String host = uri.getHost();
        HttpMethod method = request.getMethod();
        byte[] body = request.getBody();
        Map<String, String> customHeaders = request.getHeaders();

        CompletableFuture<RichHttpResponse> future = new CompletableFuture<>();

        CompletableFuture<Channel> channelFuture = connectionPool.acquire(uri.toString());
        channelFuture.whenComplete((channel, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }
            channel.attr(HttpAttributes.FUTURE_ATTRIBUTE_KEY).set(future);

DefaultFullHttpRequest nettyRequest;
            String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            if (uri.getRawQuery() != null && !uri.getRawQuery().isEmpty()) {
                path = path + "?" + uri.getRawQuery();
            }
            if (body != null) {
                nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method,
                        path, Unpooled.wrappedBuffer(body));
                nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length);
            } else {
                nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method,
                        path, Unpooled.EMPTY_BUFFER);
                nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
            }

            int port = uri.getPort();
            boolean https = "https".equalsIgnoreCase(uri.getScheme());
            boolean defaultPort = (port == -1) || (https ? port == 443 : port == 80);
            nettyRequest.headers().set(HttpHeaderNames.HOST, defaultPort ? host : host + ":" + (port == -1 ? (https ? 443 : 80) : port));
            nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            if (customHeaders != null) {
                customHeaders.forEach(nettyRequest.headers()::set);
            }

            channel.writeAndFlush(nettyRequest);

            // 请求总超时控制
            java.util.concurrent.ScheduledFuture<?> timeoutFuture = channel.eventLoop().schedule(() -> {
                if (!future.isDone()) {
                    future.completeExceptionally(new java.util.concurrent.TimeoutException("Request timeout: " + config.getRequestTimeoutMillis() + "ms"));
                    channel.close();
                }
            }, config.getRequestTimeoutMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

            future.whenComplete((response, ex) -> {
                timeoutFuture.cancel(false);
                // 确保在任何情况下都释放channel回连接池
                connectionPool.release(uri.toString(), channel);
            });

        });

        return future;
    }

    public void shutdown() {
        connectionPool.close();
        httpClient.shutdown();
    }
}
