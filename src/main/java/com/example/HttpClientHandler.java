package com.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CompletableFuture;

public class HttpClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            CompletableFuture<RichHttpResponse> future = ctx.channel().attr(HttpAttributes.FUTURE_ATTRIBUTE_KEY).get();
            try {
                int code = response.status().code();
                // 先复制内容，确保异常时也能提供响应体
                RichHttpResponse rich = RichHttpResponse.from(response);
                if (future != null) {
                    if (code >= 400) {
                        future.completeExceptionally(new HttpException(code, response.status().reasonPhrase(), rich.getBody()));
                    } else {
                        future.complete(rich);
                    }
                }
            } finally {
                // 统一释放底层 ByteBuf 引用
                response.release();
            }
        } else {
            // 如果msg不是FullHttpResponse，传递给下一个handler处理
            // 确保消息被正确释放或处理
            try {
                super.channelRead(ctx, msg);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CompletableFuture<RichHttpResponse> future = ctx.channel().attr(HttpAttributes.FUTURE_ATTRIBUTE_KEY).get();
        if (future != null) {
            future.completeExceptionally(cause);
        }
        // 记录异常信息到标准错误流，实际项目中应使用日志框架
        System.err.println("HttpClientHandler caught an exception: " + cause.getMessage());
        cause.printStackTrace();
        // 关闭channel以避免连接泄漏
        ctx.close();
    }
}
