package com.dianping.puma.api.debug;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public class SimplePumaClientDebug {

    public static void main(String[] args) {

        PumaClient client = new PumaClientConfig()
                .setClientName("lixt_test")
                .setDatabase("Puma")
                .setTables(Lists.newArrayList("ClientPosition"))
                .setServerHost("127.0.0.1:4040")
                .buildSimplePumaClient();

        while (true) {
            try {
                BinlogMessage message = client.get(1, 1, TimeUnit.SECONDS);
                BinlogInfo binlogInfo = message.getLastBinlogInfo();
                if (binlogInfo != null) {
                    long timestamp = binlogInfo.getTimestamp();
                    Date date = new Date(timestamp * 1000);
                    System.out.println(date);
                }


            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
