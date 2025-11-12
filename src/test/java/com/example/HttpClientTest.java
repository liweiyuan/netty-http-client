package com.example;

import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import com.example.NettyHttpClientCore;

public class HttpClientTest {

    private HttpClient httpClient;

    @Before
    public void setUp() {
        httpClient = new NettyHttpClientCore();
    }

    @After
    public void tearDown() {
        httpClient.shutdown();
    }

    @Test
    public void testHttpClientCreation() {
        assertNotNull("HttpClient should be created", httpClient);
        assertNotNull("Bootstrap should be created", httpClient.getBootstrap());
    }

    @Test
    public void testGetBootstrap() {
        assertNotNull("Bootstrap should not be null", httpClient.getBootstrap());
    }
}