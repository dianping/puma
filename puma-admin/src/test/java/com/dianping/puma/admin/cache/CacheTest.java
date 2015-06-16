package com.dianping.puma.admin.cache;

import java.util.Date;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.ClientAck;

public class CacheTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StateCacheService cacheService = new StateCacheService();
		ClientAck clientAck = new ClientAck();
		clientAck.setClientName("lixt");
		clientAck.setCreateDate(new Date());
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("mysql-bin.000832");
		binlogInfo.setBinlogPosition(123142170L);
		binlogInfo.setEventIndex(0);
		clientAck.setBinlogInfo(binlogInfo);
		cacheService.ayncSetKeyValue(clientAck.getClientName(), clientAck);
	}

}
