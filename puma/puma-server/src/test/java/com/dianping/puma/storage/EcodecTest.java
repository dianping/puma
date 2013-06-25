package com.dianping.puma.storage;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;

public class EcodecTest {

	private EventCodec codec = new JsonEventCodec();

	@Test
	public void test() throws Exception {
		// mock 数据
		RowChangedEvent event = new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table1");
		event.setBinlog("abc");
		event.setActionType(2);
		event.setBinlogPos(100);
		event.setExecuteTime(1023);
		event.setServerId(1340);
		event.setTransactionBegin(false);
		event.setTransactionCommit(false);
		Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
		ColumnInfo value1 = new ColumnInfo();
		value1.setKey(true);
		value1.setOldValue("1111");
		value1.setNewValue(mockBigString());
		ColumnInfo value2 = new ColumnInfo();
		value2.setKey(true);
		value2.setOldValue("1111");
		value2.setNewValue(mockBigString());
		ColumnInfo value3 = new ColumnInfo();
		value3.setKey(true);
		value3.setOldValue("1111");
		value3.setNewValue(mockBigString());
		columns.put("aaaa", value1);
		columns.put("bbbb", value2);
		columns.put("cccc", value3);
		event.setColumns(columns);
		byte[] data = codec.encode(event);

		long begin = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			codec.decode(data);
		}
		System.out.println(System.currentTimeMillis() - begin);
	}

	private String mockBigString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5024; i++) {
			sb.append(i);
		}
		return sb.toString();
	}

}
