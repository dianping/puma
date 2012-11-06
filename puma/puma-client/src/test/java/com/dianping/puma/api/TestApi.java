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

import com.dianping.puma.core.event.ChangedEvent;

/**
 * TODO Comment of TestApi
 * 
 * @author Leo Liang
 * 
 */
public class TestApi {
	public static void main(String[] args) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.ddl(true);
		configBuilder.dml(true);
		configBuilder.host("127.0.0.1");
		configBuilder.port(8080);
		configBuilder.serverId(1);
		configBuilder.name("testClient");
		configBuilder.tables("hupeng", "*");
		configBuilder.target("Dianping");
		configBuilder.transaction(false);
		configBuilder.binlog("mysql-bin.000121");
		configBuilder.binlogPos(107);
		PumaClient pc = new PumaClient(configBuilder.build());
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

				System.out.println("********************Received " + event);

			}
		});
		pc.start();
	}
}
