package com.dianping.puma.api;

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.puma.core.event.ChangedEvent;

public class PumaClientTest {
	@Test
	public void testNoConf() {
		try {
			new PumaClient(null);
			Assert.fail();
		} catch (IllegalArgumentException e) {

		}

	}

	@Test
	public void testConstruct() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.codecType("json");
		builder.ddl(true);
		builder.dml(false);
		builder.host("111.3.3");
		builder.name("test");
		builder.port(123);
		builder.seqFileBase("11111");
		builder.tables("cat", "a", "b*");
		builder.tables("me", "d");
		builder.target("fff");
		builder.transaction(true);
		PumaClient pumaClient = new PumaClient(builder.build());
		Assert.assertNotNull(getValue(pumaClient, "config"));
		Assert.assertNotNull(getValue(pumaClient, "seqFileHolder"));
		Assert.assertNotNull(getValue(pumaClient, "codec"));
		Assert.assertEquals(false, getValue(pumaClient, "active"));
	}

	@Test
	public void testReconnect() {

	}

	// puma server/ip [localhost/7862]
	// databases [all]
	// tables [all]
	// DDL & DML [both true]
	// starting sequence [0]
	// transaction supported? [false]
	// String url =
	// "http://localhost:7862/puma/channel?dt=mysql.*&dt=cat.!report&ddl=false&seq=12345&ts=true&batch=100";
	@Test
	public void testApi() throws InterruptedException {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder() //
				.host("localhost") //
				.port(7862)//
				.tables("cat", "*")//
				.ddl(true) //
				.dml(true)//
				.tables("binlog", "*")//
				.name("testClient2")//
				.target("7-43") //
				.transaction(true);

		PumaClient client = new PumaClient(configBuilder.build());

		client.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) {
				System.out.println(event);
			}
		});

		client.start();

		Thread.sleep(10000 * 1000);
		client.stop();
	}

	private Object getValue(Object obj, String fieldName) {
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				field.setAccessible(true);
				try {
					return field.get(obj);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
}
