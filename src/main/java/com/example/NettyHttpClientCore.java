package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyHttpClientCore implements HttpClient {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public NettyHttpClientCore() {
        // 根据 CPU 核心数动态配置线程数，提升吞吐并避免过度调度
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() * 2);
        this.workerGroup = new NioEventLoopGroup(threads);
        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public void shutdown() {
        workerGroup.shutdownGracefully();
    }
}
