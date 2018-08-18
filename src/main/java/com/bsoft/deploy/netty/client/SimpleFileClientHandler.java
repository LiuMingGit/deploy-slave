package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.SlaveApplication;
import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.file.FileWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 子节点接收文件传输列表
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        AppFile file = (AppFile) o;
        try {
            FileWorker fw = new FileWorker();
            if (fw.receive(file)) {
                logger.info(file.getName() + " 同步成功");
            } else {
                logger.error(file.getName() + " 同步失败");
                // todo 失败的处理流程
            }
        } catch (Exception e) {
            logger.error("{}文件同步失败!",file.getName(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            logger.error("服务连接异常!", cause);
            // ctx.channel().close();
        } else {
            logger.error("客户端处理失败!", cause);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("连接已失效,尝试重新连接...");
        //使用过程中断线重连
        ctx.channel().eventLoop().shutdownGracefully();
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                // 重连
                SimpleFileClient client = SlaveApplication.getContext().getBean(SimpleFileClient.class);
                client.start();
            }
        });
        super.channelInactive(ctx);
    }
}
