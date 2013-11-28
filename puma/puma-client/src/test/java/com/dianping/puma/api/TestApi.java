/**
 * Project: puma-client
 * 
 * File Created at 2012-8-14
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.api;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

/**
 * @author Leo Liang
 */
public class TestApi {
    public static void main(String[] args) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.ddl(false);
        configBuilder.dml(true);

        configBuilder.host("10.1.6.127");
        configBuilder.port(80);
        configBuilder.target("dianping");

        configBuilder.serverId(3013306121L);
        configBuilder.binlog("mysql-bin.003320");
        configBuilder.binlogPos(417592256L);
        configBuilder.name("testClient");

        configBuilder.tables("Dianping", "*");
        configBuilder.transaction(false);

        PumaClient pc = new PumaClient(configBuilder.build());
        pc.getSeqFileHolder().saveSeq(SubscribeConstant.SEQ_FROM_BINLOGINFO);

        pc.register(new EventListener() {

            @Override
            public void onSkipEvent(ChangedEvent event) {
                System.out.println(">>>>>>>>>>>>>>>>>>Skip " + event);
            }

            @Override
            public boolean onException(ChangedEvent event, Exception e) {
                System.out.println("-------------Exception " + e);
                return true;
            }

            @Override
            public void onEvent(ChangedEvent event) throws Exception {
                // biz logic
                if (event instanceof RowChangedEvent) {
                    RowChangedEvent rce = (RowChangedEvent) event;
                    if (rce.getTable().equals("TG_Order") && rce.getActionType() == RowChangedEvent.INSERT) {
                        System.out.println(rce);
                    }
                }

            }

            @Override
            public void onConnectException(Exception e) {
            }

            @Override
            public void onConnected() {
            }
        });
        pc.start();
    }
}
