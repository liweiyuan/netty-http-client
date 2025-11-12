package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import com.example.HttpClientConfig;

public class FullHttpRequestTest {
    private TestHttpServer testServer;
    private NettyHttpClient client;

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
        
        // 创建客户端
        client = NettyHttpClient.builder().config(HttpClientConfig.builder().poolSize(5).build()).build();
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.shutdown();
        }
        if (testServer != null) {
            testServer.stop();
        }
    }

    @Test
    public void testGetRequest() throws Exception {
        String url = "http://localhost:8080";
        
        // 发送GET请求
        CompletableFuture<RichHttpResponse> future = client.get(url);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        assertTrue("Response body should contain expected text", 
                   response.getBody().contains("Hello from TestHttpServer"));
    }

    @Test
    public void testPostRequest() throws Exception {
        String url = "http://localhost:8080";
        String postData = "test data";
        byte[] body = postData.getBytes();
        
        // 发送POST请求
        CompletableFuture<RichHttpResponse> future = client.post(url, body);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        assertTrue("Response body should contain expected text", 
                   response.getBody().contains("POST request received"));
    }

    @Test
    public void testPutRequest() throws Exception {
        String url = "http://localhost:8080";
        String putData = "put data";
        byte[] body = putData.getBytes();
        
        // 发送PUT请求
        CompletableFuture<RichHttpResponse> future = client.put(url, body);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        assertTrue("Response body should contain expected text", 
                   response.getBody().contains("PUT request received"));
    }

    @Test
    public void testDeleteRequest() throws Exception {
        String url = "http://localhost:8080";
        
        // 发送DELETE请求
        CompletableFuture<RichHttpResponse> future = client.delete(url);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        assertTrue("Response body should contain expected text", 
                   response.getBody().contains("DELETE request received"));
    }

    @Test
    public void testMultipleConcurrentRequests() throws Exception {
        String url = "http://localhost:8080";
        
        // 发送多个并发请求
        CompletableFuture<RichHttpResponse> future1 = client.get(url);
        CompletableFuture<RichHttpResponse> future2 = client.get(url);
        CompletableFuture<RichHttpResponse> future3 = client.get(url);
        
        // 等待所有请求完成
        RichHttpResponse response1 = future1.get(10, TimeUnit.SECONDS);
        RichHttpResponse response2 = future2.get(10, TimeUnit.SECONDS);
        RichHttpResponse response3 = future3.get(10, TimeUnit.SECONDS);
        
        // 验证所有响应
        assertNotNull("Response1 should not be null", response1);
        assertNotNull("Response2 should not be null", response2);
        assertNotNull("Response3 should not be null", response3);
        
        assertEquals("Response1 status should be 200", 200, response1.getStatusCode());
        assertEquals("Response2 status should be 200", 200, response2.getStatusCode());
        assertEquals("Response3 status should be 200", 200, response3.getStatusCode());
    }
}