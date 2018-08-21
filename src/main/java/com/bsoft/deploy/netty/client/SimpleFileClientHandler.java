package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.file.FileWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        logger.debug("收到服务器信息:" + o.toString());
        if (o instanceof AppFile) {
            // 处理文件
            fileReceive((AppFile) o);
        } else if (o instanceof Order) {
            Order order = (Order) o;
            orderReceive(order);
            if(order.getUniqueId() != null) {
                channelHandlerContext.channel().writeAndFlush(order);
            }
        }
    }

    /**
     * 文件接收
     *
     * @param file
     */
    private void fileReceive(AppFile file) {
        try {
            FileWorker fw = new FileWorker();
            if (fw.receive(file)) {
                logger.info(file.getName() + " 同步成功");
            } else {
                logger.error(file.getName() + " 同步失败");
                // todo 失败的处理流程
            }
        } catch (Exception e) {
            logger.error("{}文件同步失败!", file.getName(), e);
        }
    }

    /**
     * 命令执行
     *
     * @param order
     */
    private void orderReceive(Order order) {
        try {
            order.setRespData(OrderRunner.execute(order));
        } catch (Exception e) {
            logger.error("命令处理失败!", e);
            Map<String, Object> respData = new HashMap<>();
            respData.put("code", 300);
            respData.put("message", e.getMessage());
            order.setReqData(null);
            order.setRespData(respData);
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
                SimpleFileClient client = Global.getAppContext().getBean(SimpleFileClient.class);
                client.start();
            }
        });
        super.channelInactive(ctx);
    }
}
