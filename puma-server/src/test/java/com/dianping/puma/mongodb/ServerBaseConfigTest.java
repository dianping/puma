package com.dianping.puma.mongodb;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.server.dao.ServerBaseConfigDao;
import com.dianping.puma.core.server.model.ServerBaseConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.service.impl.ServerBaseConfigServiceImpl;

public class ServerBaseConfigTest {

	
private MongoClient mongoClient;
	
	@Before
	public void before(){
		mongoClient = new MongoClient();
	}
	
	@Test
	public void insert(){
		System.out.println("starting .........");
		ServerBaseConfig serverBaseConfig=new ServerBaseConfig();
		ServerBaseConfigDao dao=new ServerBaseConfigDao(mongoClient);
		ServerBaseConfigServiceImpl service = new ServerBaseConfigServiceImpl();
		
		service.setServerBaseConfigDao(dao);
		
		serverBaseConfig.setHost("10.128.38.161:8080");
		serverBaseConfig.setName("localhost:8080");
		service.save(serverBaseConfig);
		
		System.out.println("ended.............");
		
	}
}
