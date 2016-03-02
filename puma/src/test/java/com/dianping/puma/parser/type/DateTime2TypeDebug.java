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
 * datetime2(mysql 5.6) type test
 * 
 * @author qi.yin
 *
 */
public class DateTime2TypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_datetime2";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
		      + "`id` int NOT NULL AUTO_INCREMENT, \n" + "`size_dateTime1` datetime(1) NULL DEFAULT NULL, \n"
		      + "`size_dateTime2` datetime(2) NULL DEFAULT NULL, \n" + "`size_dateTime3` datetime(3) NULL DEFAULT NULL, \n"
		      + "`size_dateTime4` datetime(4) NULL DEFAULT NULL, \n" + "`size_dateTime5` datetime(5) NULL DEFAULT NULL, \n"
		      + "`size_dateTime6` datetime(6) NULL DEFAULT NULL, \n" + "`unsize_dateTime` datetime NULL DEFAULT NULL, \n"
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
	public void dateTime2TypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				String[][] testData = {
				      { "2015-06-25 17:13:56.9", "2015-06-25 17:13:56.10", "2015-06-25 17:13:56.100",
				            "2015-06-25 17:13:56.2300", "2015-06-25 17:13:56.25400", "2015-06-25 17:13:56.123400",
				            "2015-06-25 17:13:56" },
				      { "2015-06-25 17:13:56.0", "2015-06-25 17:13:56.01", "2015-06-25 17:13:56.101",
				            "2015-06-25 17:13:56.2003", "2015-06-25 17:13:56.20045", "2015-06-25 17:13:56.120034",
				            "2015-06-25 17:13:56" },
				      { "2015-06-25 17:13:56.9", "2015-06-25 17:13:56.01", "2015-06-25 17:13:56.001",
				            "2015-06-25 17:13:56.0023", "2015-06-25 17:13:56.00023", "2015-06-25 17:13:56.100500",
				            "2015-06-25 17:13:56" } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
					      + "`(size_dateTime1, size_dateTime2, size_dateTime3, size_dateTime4, size_dateTime5,"
					      + "size_dateTime6, unsize_dateTime)VALUES(?,?,?,?,?,?,?)";
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
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime1").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime1").getOldValue());
					Assert.assertEquals(testData[i][1],
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime2").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime2").getOldValue());
					Assert.assertEquals(testData[i][2],
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime3").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime3").getOldValue());
					Assert.assertEquals(testData[i][3],
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime4").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime4").getOldValue());
					Assert.assertEquals(testData[i][4],
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime5").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime5").getOldValue());
					Assert.assertEquals(testData[i][5],
					      String.valueOf(rowChangedEvent.getColumns().get("size_dateTime6").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_dateTime6").getOldValue());
					Assert.assertEquals(testData[i][6],
					      String.valueOf(rowChangedEvent.getColumns().get("unsize_dateTime").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsize_dateTime").getOldValue());
				}
			}

		});
	}
}
