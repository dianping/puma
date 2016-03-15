package com.dianping.puma.server.netty;

import com.dianping.puma.server.netty.handler.InterceptorHandler;
import com.dianping.puma.server.netty.handler.TestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class NettyEventServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();

        channelPipeline.addLast(new HttpServerCodec());
        channelPipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 32));

        TestHandler testHandler = new TestHandler();
        channelPipeline.addLast(testHandler);
    }
}
