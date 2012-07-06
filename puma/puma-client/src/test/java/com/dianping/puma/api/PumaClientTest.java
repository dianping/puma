package com.dianping.puma.api;

import org.junit.Test;

import com.dianping.puma.core.event.ChangedEvent;

public class PumaClientTest {
	// puma server/ip [localhost/7862]
	// databases [all]
	// tables [all]
	// DDL & DML [both true]
	// starting sequence [0]
	// transaction supported? [false]
	// String url =
	// "http://localhost:7862/puma/channel?dt=mysql.*&dt=cat.!report&ddl=false&seq=12345&ts=true&batch=100";
	@Test
	public void testApi() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder() //
				.host("localhost") //
				.port(7862)//
				.tables("mysql", "*") //
				.tables("cat", "!report", "!tmp")//
				.ddl(false) //
				.transaction(true);

		PumaClient client = new PumaClient(configBuilder.build());

		client.register(new EventListener() {
			@Override
			public void onEvent(ChangedEvent event) {
			}
		});

		client.start();
		
		
		client.stop();
	}
}
