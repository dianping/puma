package com.dianping.puma.parser.type;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.parser.AbstractBaseDebug;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/***
 * timestamp type test
 * 
 * @author qi.yin
 *
 */
public class TimestampTypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_timestamp";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
				+ "`id` int NOT NULL AUTO_INCREMENT, \n" + "`default_timestamp` timestamp, \n" + "PRIMARY KEY (`id`)"
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
	public void timestampTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				String[][] testData = { { "2015-06-25 17:13:56" }, { "2014-03-25 17:13:56" }, { "1988-06-25 17:13:56" } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
							+ "`(default_timestamp)VALUES(?)";
					queryRunner.update(insert_SQL, testData[i][0]);
				}
				List<ChangedEvent> events = getEvents(testData.length, false, true, false);
				Assert.assertEquals(testData.length, events.size());
				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				for (int i = 0; i < testData.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.INSERT, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(2, rowChangedEvent.getColumns().size());
					String tempDate = "";
					try {
						tempDate = sdf.format(rowChangedEvent.getColumns().get("default_timestamp").getNewValue());
					} catch (IllegalArgumentException e) {
						tempDate = String.valueOf(rowChangedEvent.getColumns().get("default_timestamp").getNewValue());
					}
					Assert.assertEquals(testData[i][0], tempDate);
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("default_timestamp").getOldValue());
				}
			}

		});
	}
}
