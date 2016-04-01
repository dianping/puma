package com.dianping.puma.consumer.netty;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class NettyEventServerIntegrationTest {

    public static void main(String[] args) {
        NettyEventServer nettyEventServer = new NettyEventServer();
        nettyEventServer.setEpoll(false);
        nettyEventServer.start();

        Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MINUTES);
    }

}