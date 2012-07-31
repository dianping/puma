package com.dianping.puma.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class DataTypeIntegrationTest extends PumaServerIntegrationBaseTest {
	private String	table	= "dataTypeTest";

	@Before
	public void before() throws Exception {
		executeSql("DROP TABLE IF EXISTS " + table);
	}
	
	@Test
	public void testBigInt() throws Exception {
		executeSql("CREATE TABLE " + table + "(id BIGINT)");
		executeSql("INSERT INTO " + table + " values(1)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof Long);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getNewValue().equals(new Long(2)));
				Assert.assertTrue (rowChangedEvent.getColumns().get("id").getOldValue().equals(new Long(1)));
			}
		});
	}
	
	@Test
	public void testBinary() throws Exception {
		executeSql("CREATE TABLE " + table + "(id BINARY(8))");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				byte[] data = new byte[]{1,2,3,54,5,6,67};
				insertWithBinaryColumn("INSERT INTO " + table + " values(?)", data);
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getNewValue() instanceof String);
				Assert.assertEquals(new String(data), (String)rowChangedEvent.getColumns().get("id").getNewValue());
			}
		});
	}
	
	@Test
	public void testBit() throws Exception {
		executeSql("CREATE TABLE " + table + "(id BIT(8))");
		executeSql("INSERT INTO " + table + " values(11)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=10 WHERE id=11");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof byte[]);
				Assert.assertArrayEquals(new byte[]{11}, (byte[])rowChangedEvent.getColumns().get("id").getOldValue());
				Assert.assertArrayEquals(new byte[]{10}, (byte[])rowChangedEvent.getColumns().get("id").getNewValue());
			}
		});
	}
	
	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}

}
