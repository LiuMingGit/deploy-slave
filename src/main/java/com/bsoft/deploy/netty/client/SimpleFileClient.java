package com.bsoft.deploy.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileClient {
    private final static Logger logger = LoggerFactory.getLogger(SimpleFileClient.class);

    @Value("${netty.server.host}")
    private String host;
    @Value("${netty.server.port}")
    private int port;

    private int reConnectCount = 0;

    public void start() {
        try {
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new SimpleFileClientInitializer());
            ChannelFuture f = bootstrap.connect(host, port).sync();
            f.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        reConnectCount = 0;
                        logger.info("***********connect to netty server {}:{} success", host, port);
                    } else {
                        reConnect();
                    }

                }
            });
        } catch (Exception e) {
            logger.error("netty server {}:{} connection error...", host, port);
            reConnect();
        }
    }

    /**
     * 断线重连
     */
    private void reConnect() {
        try {
            int delay = ++reConnectCount * 5;
            reConnectCount = reConnectCount > 23 ? 23 : reConnectCount;
            logger.error("与服务器{}:{}连接已断开, {}秒后重连...", host, port, delay);

            Thread.sleep(delay * 1000);
            start();
        } catch (Exception e) {
            logger.error("发生了预期外的异常,请及时处理!", e);
        }
    }

}
