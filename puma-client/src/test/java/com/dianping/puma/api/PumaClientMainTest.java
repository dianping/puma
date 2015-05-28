package com.dianping.puma.api;

import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.event.ChangedEvent;


public class PumaClientMainTest {
	
	public PumaClient createPumaClient() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.host("192.168.224.101");
		configBuilder.port(8080);
		configBuilder.name("ClientTest");
		configBuilder.serverId(1);
		configBuilder.target("DianPing_beta@server_beta");
		configBuilder.dml(true);
		configBuilder.ddl(true);
		configBuilder.transaction(true);
		configBuilder.binlog("mysql-bin.000001");
		configBuilder.binlogPos(1);
		configBuilder.tables("DianPing", "DP_RegionList","POI_FeedBack");
		//_parseSourceDatabaseTables(task.getMysqlMapping(), configBuilder);
		Configuration configuration = configBuilder.build();

		final PumaClient pumaClient = new PumaClient(configuration);
		pumaClient.getSeqFileHolder().saveSeq(-2L);

		pumaClient.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) throws Exception {
				System.out.println(event.toString());
			}

			@Override
			public boolean onException(ChangedEvent event, Exception e) {
				return false;
			}

			@Override
			public void onConnectException(Exception e) {
				
			}

			@Override
			public void onConnected() {
				
			}

			@Override
			public void onSkipEvent(ChangedEvent event) {
			}
		});

		return pumaClient;
	}
	
	public static void main(String []args){
		PumaClientMainTest main = new PumaClientMainTest();
		PumaClient pumaClient = main.createPumaClient();
		pumaClient.start();
		
	}
}
