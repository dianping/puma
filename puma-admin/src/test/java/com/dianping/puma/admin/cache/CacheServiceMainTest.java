package com.dianping.puma.admin.cache;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.puma.admin.common.StateContainer;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.ClientAck;

public class CacheServiceMainTest {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
//		StateContainer stateContainer = (StateContainer) applicationContext.getBean("stateContainer");
//		ClientAck clientAck = new ClientAck();
//		clientAck.setClientName("lixt");
//		clientAck.setCreateDate(new Date());
//		BinlogInfo binlogInfo = new BinlogInfo();
//		binlogInfo.setBinlogFile("mysql-bin.000832");
//		binlogInfo.setBinlogPosition(123142170L);
//		binlogInfo.setEventIndex(0);
//		clientAck.setBinlogInfo(binlogInfo);
//		stateContainer.setClientAckInfo(clientAck);
//		
		StateCacheService stateCacheService = (StateCacheService) applicationContext.getBean("stateCacheService");
		stateCacheService.ayncSetKeyValue("lixt_client", null);
		
		
		
	}

}
