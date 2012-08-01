package com.dianping.puma.integration;

import java.text.SimpleDateFormat;
import java.util.Date;
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
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue().equals(new Long(1)));
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
				byte[] data = new byte[] { 1, 2, 3, 54, 5, 6, 67 };
				insertWithBinaryColumn("INSERT INTO " + table + " values(?)", data);
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getNewValue() instanceof String);
				Assert.assertEquals(new String(data), (String) rowChangedEvent.getColumns().get("id").getNewValue());
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
				Assert.assertArrayEquals(new byte[] { 11 }, (byte[]) rowChangedEvent.getColumns().get("id")
						.getOldValue());
				Assert.assertArrayEquals(new byte[] { 10 }, (byte[]) rowChangedEvent.getColumns().get("id")
						.getNewValue());
			}
		});
	}

	@Test
	public void testBool() throws Exception {
		executeSql("CREATE TABLE " + table + "(id BOOL)");
		executeSql("INSERT INTO " + table + " values(11)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=10 WHERE id=11");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof Integer);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue().equals( new Integer(11)));
				Assert.assertTrue(rowChangedEvent.getColumns().get("id")
						.getNewValue().equals(new Integer(10)));
			}
		});
	}
	
	@Test
	public void testBoolean() throws Exception {
		executeSql("CREATE TABLE " + table + "(id BOOLEAN)");
		executeSql("INSERT INTO " + table + " values(11)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=10 WHERE id=11");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof Integer);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue().equals( new Integer(11)));
				Assert.assertTrue(rowChangedEvent.getColumns().get("id")
						.getNewValue().equals(new Integer(10)));
			}
		});
	}
	
	@Test
	public void testChar() throws Exception {
		executeSql("CREATE TABLE " + table + "(id CHAR)");
		executeSql("INSERT INTO " + table + " values('a')");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id='b' WHERE id='a'");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof String);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue().equals( new String("a")));
				Assert.assertTrue(rowChangedEvent.getColumns().get("id")
						.getNewValue().equals(new String("b")));
			}
		});
	}
	
	@Test
	public void testDate() throws Exception {
		executeSql("CREATE TABLE " + table + "(id DATE)");
		executeSql("INSERT INTO " + table + " values('2012-01-1')");
		waitForSync(50);
		test(new TestLogic() {

			@SuppressWarnings("deprecation")
			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id='2013-09-11' WHERE id='2012-01-01'");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof Date);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse("2012-01-01");
				Date oldvalue= (Date) rowChangedEvent.getColumns().get("id").getOldValue();
				Assert.assertEquals(date.getDate(),oldvalue.getDate());
				Assert.assertEquals(date.getMonth(),oldvalue.getMonth());
				Assert.assertEquals(date.getYear(), oldvalue.getYear());
				Date newdate = sdf.parse("2013-09-11");
				Date newvalue= (Date) rowChangedEvent.getColumns().get("id").getNewValue();
				Assert.assertEquals(newdate.getDate(),newvalue.getDate());
				Assert.assertEquals(newdate.getMonth(),newvalue.getMonth());
				Assert.assertEquals(newdate.getYear(), newvalue.getYear());
			}
		});
	}
	
	@Test
	public void testDateTime() throws Exception {
		executeSql("CREATE TABLE " + table + "(id DATETIME)");
		executeSql("INSERT INTO " + table + " values('2009-9-9 23:22:11')");
		waitForSync(50);
		test(new TestLogic() {
			@SuppressWarnings("deprecation")
			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id='2010-10-10 22:11:22' WHERE id='2009-9-9 23:22:11'");
				List<ChangedEvent> events = getEvents(1, false);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.getColumns().get("id").getOldValue() instanceof Date);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = sdf.parse("2009-9-9 23:22:11");
				Date oldvalue= (Date) rowChangedEvent.getColumns().get("id").getOldValue();
				Assert.assertEquals(date.getDate(),oldvalue.getDate());
				Assert.assertEquals(date.getMonth(),oldvalue.getMonth());
				Assert.assertEquals(date.getYear(), oldvalue.getYear());
				Assert.assertEquals(date.getHours(), oldvalue.getHours());
				Assert.assertEquals(date.getMinutes(), oldvalue.getMinutes());
				Assert.assertEquals(date.getSeconds(), oldvalue.getSeconds());
				Date newdate = sdf.parse("2010-10-10 22:11:22");
				Date newvalue= (Date) rowChangedEvent.getColumns().get("id").getNewValue();
				Assert.assertEquals(newdate.getDate(),newvalue.getDate());
				Assert.assertEquals(newdate.getMonth(),newvalue.getMonth());
				Assert.assertEquals(newdate.getYear(), newvalue.getYear());
				Assert.assertEquals(newdate.getHours(), newvalue.getHours());
				Assert.assertEquals(newdate.getMinutes(), newvalue.getMinutes());
				Assert.assertEquals(newdate.getSeconds(), newvalue.getSeconds());
			}
			
		});
	}

	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}

}
