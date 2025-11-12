package com.example;

import io.netty.bootstrap.Bootstrap;

public interface HttpClient {
    Bootstrap getBootstrap();
    void shutdown();
}