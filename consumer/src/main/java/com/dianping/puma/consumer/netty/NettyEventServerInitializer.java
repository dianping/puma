package com.dianping.puma.consumer.netty;

import com.dianping.puma.consumer.netty.decode.EventRequestDecoder;
import com.dianping.puma.consumer.netty.handler.HttpRequestHandler;
import com.dianping.puma.consumer.netty.handler.TestHandler;
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

    private EventRequestDecoder eventRequestDecoder;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();

        // Http request decode and response encode.
        HttpServerCodec httpServerCodec = new HttpServerCodec();
        channelPipeline.addLast(httpServerCodec);

        // Http object aggregator.
        HttpObjectAggregator httpObjectAggregator = new HttpObjectAggregator(1024 * 1024 * 32);
        channelPipeline.addLast(httpObjectAggregator);

        // Http request handler.
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        httpRequestHandler.setDecoder(eventRequestDecoder);
        channelPipeline.addLast(httpRequestHandler);

        TestHandler testHandler = new TestHandler();
        channelPipeline.addLast(testHandler);
    }

    public void setEventRequestDecoder(EventRequestDecoder eventRequestDecoder) {
        this.eventRequestDecoder = eventRequestDecoder;
    }
}
