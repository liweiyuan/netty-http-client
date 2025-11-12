package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionPool {
    private final Bootstrap bootstrap;
    private final int poolSize;
    private final HttpClientConfig config;
    private final ConcurrentMap<String, ChannelPool> poolMap = new ConcurrentHashMap<>();

    public ConnectionPool(Bootstrap bootstrap, HttpClientConfig config) {
        this.bootstrap = bootstrap;
        this.poolSize = config.getPoolSize();
        this.config = config;
    }

    public CompletableFuture<Channel> acquire(String url) {
        try {
            URI uri = URI.create(url);
            String key = uri.getHost() + ":" + (uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort());
            ChannelPool channelPool = poolMap.computeIfAbsent(key, k -> createChannelPool(uri));
            
            CompletableFuture<Channel> future = new CompletableFuture<>();
            Future<Channel> channelFuture = channelPool.acquire();
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    future.complete((Channel) f.getNow());
                } else {
                    future.completeExceptionally(f.cause());
                }
            });
            return future;
        } catch (Exception e) {
            CompletableFuture<Channel> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

private ChannelPool createChannelPool(URI uri) {
        final boolean https = "https".equalsIgnoreCase(uri.getScheme());
        return new FixedChannelPool(
                bootstrap.remoteAddress(uri.getHost(), uri.getPort() == -1 ? (https ? 443 : 80) : uri.getPort()),
                new AbstractChannelPoolHandler() {
                    @Override
                    public void channelCreated(Channel ch) throws Exception {
                        // HTTPS 支持
                        if (https) {
                            io.netty.handler.ssl.SslContext sslCtx = io.netty.handler.ssl.SslContextBuilder.forClient().build();
                            ch.pipeline().addFirst("ssl", sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort() == -1 ? 443 : uri.getPort()));
                        }
                        // 超时处理
                        ch.pipeline().addLast("readTimeout", new io.netty.handler.timeout.ReadTimeoutHandler(config.getReadTimeoutMillis(), java.util.concurrent.TimeUnit.MILLISECONDS));
                        ch.pipeline().addLast("writeTimeout", new io.netty.handler.timeout.WriteTimeoutHandler(config.getWriteTimeoutMillis(), java.util.concurrent.TimeUnit.MILLISECONDS));
                        // HTTP 编解码与聚合
                        ch.pipeline().addLast(new io.netty.handler.codec.http.HttpClientCodec());
                        ch.pipeline().addLast(new io.netty.handler.codec.http.HttpObjectAggregator(1024 * 1024 * 5));
                        ch.pipeline().addLast(new HttpClientHandler());
                    }
                },
                poolSize
        );
    }

public void release(String url, Channel channel) {
        try {
            URI uri = URI.create(url);
            String key = uri.getHost() + ":" + (uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort());
            ChannelPool channelPool = poolMap.get(key);
            if (channelPool != null && channel.isActive()) {
                channelPool.release(channel);
            } else {
                // 不可用的连接直接关闭
                channel.close();
            }
        } catch (Exception e) {
            // 忽略释放时的异常
            try { channel.close(); } catch (Exception ignored) {}
        }
    }

    public void close() {
        for (ChannelPool pool : poolMap.values()) {
            pool.close();
        }
        poolMap.clear();
    }
}
