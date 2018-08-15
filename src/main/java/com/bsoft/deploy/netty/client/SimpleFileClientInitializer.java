package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.netty.codec.MarshallingCodeCFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //framer  DelimiterBasedFrameDecoder  表示结束帧的标记 /r/n
        pipeline.addLast("decoder", MarshallingCodeCFactory.buildMarshallingDecoder());
        pipeline.addLast("encoder", MarshallingCodeCFactory.buildMarshallingEncoder());
        pipeline.addLast("handler", new SimpleFileClientHandler());
    }
}
