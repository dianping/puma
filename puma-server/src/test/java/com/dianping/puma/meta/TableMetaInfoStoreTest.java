package com.dianping.puma.meta;

import java.io.IOException;

import org.junit.Test;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.meta.TableMetaInfo;
import com.dianping.puma.core.model.BinlogInfo;

public class TableMetaInfoStoreTest {
	
	
	@Test
	public void addMetaInfo() throws IOException{
		TableMetaInfoStore store = new TableMetaInfoStore();
		store.start();
		
		DdlEvent event = new DdlEvent();
		
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile("mysql-bin.000001");
		binlogInfo.setBinlogPosition(5376L);
		
		event.setBinlogInfo(binlogInfo);
		event.setBinlogServerId(1L);
		
		TableMetaInfo tableMetaInfo = new TableMetaInfo();
		tableMetaInfo.setDatabase("Test");
		tableMetaInfo.setTable("test1");
		
		store.addTableMetaInfo(event , tableMetaInfo);
		
		RowChangedEvent rowEvent = new RowChangedEvent();
		rowEvent.setDatabase("Test");
		rowEvent.setTable("test1");
		BinlogInfo binlogInfo1 = new BinlogInfo();
		binlogInfo1.setBinlogFile("mysql-bin.000002");
		binlogInfo1.setBinlogPosition(5375L);
		
		rowEvent.setBinlogInfo(binlogInfo1);
		rowEvent.setBinlogServerId(1L);
		
		TableMetaInfo target = store.getTableMetaInfo(rowEvent);
		
		System.out.println(target.getDatabase());
		System.out.println(target.getTable());
	}

}
