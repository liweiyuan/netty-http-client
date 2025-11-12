package com.example;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class HttpClientHandlerTest {

    private EmbeddedChannel channel;
    private HttpClientHandler handler;

    @Before
    public void setUp() {
        handler = new HttpClientHandler();
        channel = new EmbeddedChannel(handler);
    }

    @After
    public void tearDown() {
        channel.finishAndReleaseAll();
    }

    @Test
    public void testChannelReadWithFullHttpResponse() {
        // 创建一个CompletableFuture来接收结果
        CompletableFuture<RichHttpResponse> future = new CompletableFuture<>();
        channel.attr(HttpAttributes.FUTURE_ATTRIBUTE_KEY).set(future);

        // 创建一个模拟的HTTP响应
        String content = "Hello, World!";
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                io.netty.buffer.Unpooled.copiedBuffer(content, StandardCharsets.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length());

        // 将响应写入channel
        channel.writeInbound(response);

        // 验证future是否完成并包含正确的响应内容
        assertTrue("Future should be completed", future.isDone());
        assertFalse("Future should not be completed exceptionally", future.isCompletedExceptionally());

        try {
            RichHttpResponse richResponse = future.get();
            assertNotNull("RichHttpResponse should not be null", richResponse);
            assertEquals("Response body should match", content, richResponse.getBody());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testExceptionCaught() throws Exception {
        // 创建一个CompletableFuture来接收异常
        CompletableFuture<RichHttpResponse> future = new CompletableFuture<>();
        channel.attr(HttpAttributes.FUTURE_ATTRIBUTE_KEY).set(future);

        // 模拟一个异常
        Exception exception = new RuntimeException("Test exception");
        handler.exceptionCaught(channel.pipeline().firstContext(), exception);

        // 验证future是否以异常完成
        assertTrue("Future should be completed", future.isDone());
        assertTrue("Future should be completed exceptionally", future.isCompletedExceptionally());
    }
}