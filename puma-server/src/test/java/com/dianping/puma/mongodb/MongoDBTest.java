package com.dianping.puma.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.DefaultNotifyService;
import com.dianping.puma.core.server.dao.PumaServerDetailConfigDao;
import com.dianping.puma.core.server.model.FileSenderConfig;
import com.dianping.puma.core.server.model.PumaServerDetailConfig;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.dianping.puma.sender.dispatcher.SimpleDispatherImpl;
import com.dianping.puma.server.MMapBasedBinlogPositionHolder;
import com.dianping.puma.service.impl.PumaServerConfigServiceImpl;


public class MongoDBTest {
	
	private MongoClient mongoClient;
	
	@Before
	public void before(){
		mongoClient = new MongoClient();
	}
	
	@Test
	public void insert(){
		System.out.println("starting .........");
		PumaServerDetailConfig config=new PumaServerDetailConfig();
		PumaServerDetailConfigDao dao=new PumaServerDetailConfigDao(mongoClient);
		PumaServerConfigServiceImpl service = new PumaServerConfigServiceImpl();
		service.setPumaServerDetailConfigDao(dao);
		
		config.setWebAppName("webApp_1");
		config.setDbHost("127.0.0.1");
		config.setDbPort(3306);
		config.setDbPassword("admin");
		config.setDbUser("root");
		config.setDefaultBinlogFileName("mysql-bin.000002");
		config.setDefaultBinlogPosition(106);
		config.setDbServerId(1);
		config.setServerId(9988);
		config.setServerName("server-7_43");
		config.setMetaDBHost("127.0.0.1");
		config.setMetaDBPort(3306);
		config.setMetaDBPassword("admin");
		config.setMetaDBUser("root");
		config.setDispatcherName("dispatcher-7_43");
		List<FileSenderConfig> fileConfigs=new ArrayList<FileSenderConfig>();
		FileSenderConfig fileConfig=new FileSenderConfig();
		fileConfig.setBinlogIndexBaseDir("/data/appdatas/puma/binlogIndex/7_43/");
		fileConfig.setStorageAcceptedTablesConfigKey("puma.7_43_acceptedTables");
		fileConfig.setMasterBucketFilePrefix("bucket-");
		fileConfig.setMaxMasterBucketLengthMB(1000);
		fileConfig.setStorageMasterBaseDir("/data/appdatas/puma/storage/master/7_43/");
		
		fileConfig.setSlaveBucketFilePrefix("bucket-");
		fileConfig.setMaxSlaveBucketLengthMB(1000);
		fileConfig.setStorageSlaveBaseDir("/data/appdatas/puma/storage/slave/7_43/");
		
		fileConfig.setMaxMasterFileCount(1);
		fileConfig.setPreservedDay(14);
		fileConfig.setStorageName("storage-7_43");
		
		fileConfigs.add(fileConfig);
		
		config.setFileSenders(fileConfigs);
		service.save(config);
		
		
		//DefaultNotifyService notifyService = new DefaultNotifyService();
		
		//MMapBasedBinlogPositionHolder binlogPositionHolder =new MMapBasedBinlogPositionHolder();
		
		//binlogPositionHolder.setBaseDir("/data/appdatas/puma/");
		
		//JsonEventCodec jsonCodec=new JsonEventCodec();
		
		System.out.println("ended.............");
		
	}

}
