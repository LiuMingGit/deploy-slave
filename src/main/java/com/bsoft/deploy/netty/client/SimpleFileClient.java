package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.dao.entity.AppFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileClient {
    private final static Logger logger = LoggerFactory.getLogger(SimpleFileClient.class);
    private EventLoopGroup group = new NioEventLoopGroup();
    private String host;
    private int port;
    private int reConnectCount = 0;

    public SimpleFileClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
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
                        logger.info("connect to netty server {}:{} success", host, port);
                    } else {
                        reConnect();
                    }

                }
            });
            Channel channel = f.channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = in.readLine();
                System.out.println(input);
                AppFile file = new AppFile("D:/workspace_ideal/deploy/master/pom.xml", "D:/workspace_ideal/deploy/master/");
                channel.writeAndFlush(file);
            }
        } catch (Exception e) {
            logger.error("netty server {}:{} connection error...", host, port);
        } finally {
            reConnect();
            group.shutdownGracefully();
        }
    }

    /**
     * 断线重连
     */
    private void reConnect() {
        // fixme: 重连显式退出?
        try {
            int delay = ++reConnectCount * 5;
            reConnectCount = reConnectCount > 23 ? 23 : reConnectCount;
            logger.error("与服务器{}:{}连接已断开, {}秒后重连...", host, port, delay);

            Thread.sleep(delay * 1000);
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
