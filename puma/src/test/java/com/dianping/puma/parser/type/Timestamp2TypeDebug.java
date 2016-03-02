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
 * timestamp2(mysql 5.6) type test
 * 
 * @author qi.yin
 *
 */
public class Timestamp2TypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_timestamp2";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
		      + "`id` int NOT NULL AUTO_INCREMENT, \n" + "`size_timestamp1` timestamp(1) NULL default NULL, \n"
		      + "`size_timestamp2` timestamp(2) NULL default NULL, \n" + "`size_timestamp3` timestamp(3) NULL default NULL, \n"
		      + "`size_timestamp4` timestamp(4) NULL default NULL, \n" + "`size_timestamp5` timestamp(5) NULL default NULL, \n"
		      + "`size_timestamp6` timestamp(6) NULL default NULL, \n" + "`unsize_timestamp` timestamp NULL default NULL, \n" + "PRIMARY KEY (`id`)"
		      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
		queryRunner.update(create_SQL);
		setFilterTable(TABLE_NAME);
	}

	@AfterClass
	public static void doAfter() throws Exception {
		String drop_SQL = "DROP TABLE IF EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "`";
		queryRunner.update(drop_SQL);
	}

	@Test
	public void timestamp2TypeInsertTest() throws Exception {
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
					      + "`(size_timestamp1, size_timestamp2, size_timestamp3, size_timestamp4, size_timestamp5,"
					      + "size_timestamp6, unsize_timestamp)VALUES(?,?,?,?,?,?,?)";
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
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp1").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp1").getOldValue());
					Assert.assertEquals(testData[i][1],
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp2").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp2").getOldValue());
					Assert.assertEquals(testData[i][2],
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp3").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp3").getOldValue());
					Assert.assertEquals(testData[i][3],
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp4").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp4").getOldValue());
					Assert.assertEquals(testData[i][4],
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp5").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp5").getOldValue());
					Assert.assertEquals(testData[i][5],
					      String.valueOf(rowChangedEvent.getColumns().get("size_timestamp6").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_timestamp6").getOldValue());
					Assert.assertEquals(testData[i][6],
					      String.valueOf(rowChangedEvent.getColumns().get("unsize_timestamp").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsize_timestamp").getOldValue());
				}
			}

		});
	}
}
