package com.dianping.puma.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DecoderableQueueTest {

	private EventCodec codec = new JsonEventCodec();
	protected DecoderableQueue queue = new DecoderableQueueImpl(codec);
	int putCount = 0;
	int takeCount = 0;

	@Before
	public void before() {
		Fetcher f = new Fetcher();
		f.start();
		Stat stat = new Stat();
		stat.start();
	}

	@Test
	public void test() throws Exception {
		long timeout = 500;
		while (true) {
			DecoderElement ele = queue.take(timeout);
			if (ele != null) {
				++takeCount;
			}

			// 模拟输出到客户端的耗时
			TimeUnit.MILLISECONDS.sleep(50);
		}
	}

	// 模拟从本地获取到byte[] data，然后放入queue
	private class Fetcher extends Thread {

		@Override
		public void run() {
			try {
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

				while (true) {
					// 放入queue
					DecoderElement element = new DecoderElement();
					element.setData(data);

					queue.put(element);

					// 测试用的统计
					putCount++;

					// 模拟从本地文件获取data时的耗时
					TimeUnit.MILLISECONDS.sleep(40);
				}
			} catch (StorageClosedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private class Stat extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					// 监控queue的大小，判断其堆积情况
					System.out.println("queue size=" + queue.size()
							+ ", putCount=" + putCount + ", takeCount="
							+ takeCount);

					TimeUnit.MILLISECONDS.sleep(2000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private String mockBigString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5024; i++) {
			sb.append(i);
		}
		return sb.toString();
	}

}
