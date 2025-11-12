package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConnectionPoolTest {

    private NioEventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private ConnectionPool connectionPool;
    private TestHttpServer testServer;

    @Before
    public void setUp() throws Exception {
        // 启动测试服务器
        testServer = new TestHttpServer();
        testServer.start(8080);
        
        // 等待服务器启动
        long start = System.currentTimeMillis();
        while (!testServer.isRunning() && (System.currentTimeMillis() - start) < 5000) {
            Thread.sleep(100);
        }
        
        // 确保服务器已启动
        assertTrue("Test server should be running", testServer.isRunning());
        
        // 设置Netty客户端
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class);
        HttpClientConfig config = HttpClientConfig.builder().poolSize(5).build();
        connectionPool = new ConnectionPool(bootstrap, config);
    }

    @After
    public void tearDown() throws Exception {
        connectionPool.close();
        eventLoopGroup.shutdownGracefully();
        testServer.stop();
    }

    @Test
    public void testPoolCreation() {
        assertNotNull("ConnectionPool should be created", connectionPool);
    }

    @Test
    public void testAcquireAndRelease() throws Exception {
        String url = "http://localhost:8080";
        
        // 获取连接
        CompletableFuture<Channel> future = connectionPool.acquire(url);
        Channel channel = future.get(5, TimeUnit.SECONDS);
        assertNotNull("Channel should not be null", channel);
        
        // 释放连接
        connectionPool.release(url, channel);
    }

    @Test
    public void testMultipleUrls() throws Exception {
        String url1 = "http://localhost:8080";
        String url2 = "http://localhost:8080/test";
        
        // 为不同URL获取连接
        CompletableFuture<Channel> future1 = connectionPool.acquire(url1);
        CompletableFuture<Channel> future2 = connectionPool.acquire(url2);
        
        Channel channel1 = future1.get(5, TimeUnit.SECONDS);
        Channel channel2 = future2.get(5, TimeUnit.SECONDS);
        
        assertNotNull("Channel1 should not be null", channel1);
        assertNotNull("Channel2 should not be null", channel2);
        
        // 释放连接
        connectionPool.release(url1, channel1);
        connectionPool.release(url2, channel2);
    }

    @Test
    public void testPoolClose() throws Exception {
        String url = "http://localhost:8080";
        
        // 先获取一个连接
        CompletableFuture<Channel> future = connectionPool.acquire(url);
        Channel channel = future.get(5, TimeUnit.SECONDS);
        connectionPool.release(url, channel);
        
        // 关闭连接池
        connectionPool.close();
    }


}