package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ExternalHttpServiceTest {

    public static void main(String[] args) throws Exception {
        ExternalHttpServiceTest test = new ExternalHttpServiceTest();
        test.setUp();
        test.testGetRequestToExternalService();
        test.testGetSpecificFileFromExternalService();
        test.tearDown();
    }


    private NettyHttpClient client;



    public void setUp() {
        // 创建客户端，连接到外部HTTP服务（Python启动的服务）
        client = NettyHttpClient.builder()
                .config(HttpClientConfig.builder().poolSize(5).build())
                .build();
    }


    public void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }


    public void testGetRequestToExternalService() throws Exception {
        // 连接到由Python启动的外部HTTP服务
        String url = "http://localhost:8081";
        
        // 发送GET请求
        CompletableFuture<RichHttpResponse> future = client.get(url);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        // Python服务器返回的内容会有所不同
        assertTrue("Response body should contain expected text",
                response.getBody().contains("Hello from Python Test Server") ||
                        response.getBody().contains("Directory listing"));
    }


    public void testGetSpecificFileFromExternalService() throws Exception {
        // 请求特定的文件
        String url = "http://localhost:8081/index.html";
        
        // 发送GET请求
        CompletableFuture<RichHttpResponse> future = client.get(url);
        RichHttpResponse response = future.get(10, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull("Response should not be null", response);
        assertEquals("Response status should be 200", 200, response.getStatusCode());
        assertTrue("Response body should contain expected text",
                response.getBody().contains("Hello from Python Test Server"));
    }
}