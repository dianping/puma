package com.dianping.puma.parser.type;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.parser.AbstractBaseDebug;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/***
 * time2(mysql 5.6) type test
 * 
 * @author qi.yin
 *
 */
public class Time2TypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_time2";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
				+ "`id` int NOT NULL AUTO_INCREMENT, \n" + "`size_time1` time(1) DEFAULT NULL, \n"
				+ "`size_time2` time(2) DEFAULT NULL, \n" + "`size_time3` time(3) DEFAULT NULL, \n"
				+ "`size_time4` time(4) DEFAULT NULL, \n" + "`size_time5` time(5) DEFAULT NULL, \n"
				+ "`size_time6` time(6) DEFAULT NULL, \n" + "`unsize_time` time DEFAULT NULL, \n"
				+ "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
		queryRunner.update(create_SQL);
		setFilterTable(TABLE_NAME);
	}

	@AfterClass
	public static void doAfter() throws Exception {
		String drop_SQL = "DROP TABLE IF EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "`";
		queryRunner.update(drop_SQL);
	}

	@Test
	public void time2TypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				String[][] testData = {
						{ "17:13:56.9", "17:13:56.10", "17:13:56.100", "17:13:56.2300", "17:13:56.25400",
								"17:13:56.123400", "17:13:56" },
						{ "17:13:56.0", "17:13:56.01", "17:13:56.101", "17:13:56.2003", "17:13:56.20045",
								"17:13:56.120034", "17:13:56" },
						{ "17:13:56.9", "17:13:56.01", "17:13:56.001", "17:13:56.0023", "17:13:56.00023",
								"17:13:56.100500", "17:13:56" } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
							+ "`(size_time1, size_time2, size_time3, size_time4, size_time5,"
							+ "size_time6, unsize_time)VALUES(?,?,?,?,?,?,?)";
					queryRunner.update(insert_SQL, testData[i][0], testData[i][1], testData[i][2], testData[i][3],
							testData[i][4], testData[i][5], testData[i][6]);
				}
				List<ChangedEvent> events = getEvents(testData.length, false, true, false);
				Assert.assertEquals(testData.length, events.size());
				for (int i = 0; i < testData.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.INSERT, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(8, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testData[i][0],
							String.valueOf(rowChangedEvent.getColumns().get("size_time1").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time1").getOldValue());
					Assert.assertEquals(testData[i][1],
							String.valueOf(rowChangedEvent.getColumns().get("size_time2").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time2").getOldValue());
					Assert.assertEquals(testData[i][2],
							String.valueOf(rowChangedEvent.getColumns().get("size_time3").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time3").getOldValue());
					Assert.assertEquals(testData[i][3],
							String.valueOf(rowChangedEvent.getColumns().get("size_time4").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time4").getOldValue());
					Assert.assertEquals(testData[i][4],
							String.valueOf(rowChangedEvent.getColumns().get("size_time5").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time5").getOldValue());
					Assert.assertEquals(testData[i][5],
							String.valueOf(rowChangedEvent.getColumns().get("size_time6").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_time6").getOldValue());
					Assert.assertEquals(testData[i][6],
							String.valueOf(rowChangedEvent.getColumns().get("unsize_time").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsize_time").getOldValue());
				}
			}

		});
	}
}
