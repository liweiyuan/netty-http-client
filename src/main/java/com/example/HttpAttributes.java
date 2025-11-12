package com.example;

import io.netty.util.AttributeKey;

import java.util.concurrent.CompletableFuture;

public final class HttpAttributes {
    public static final AttributeKey<CompletableFuture<RichHttpResponse>> FUTURE_ATTRIBUTE_KEY = AttributeKey.valueOf("future");
}
