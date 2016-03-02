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
 * year type test
 * 
 * @author qi.yin
 *
 */
public class YearTypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_year";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
		      + "`id` int NOT NULL AUTO_INCREMENT, \n" + "`size_year` year(4) NULL DEFAULT NULL, \n"
		      + "`unsize_year` year NULL DEFAULT NULL, \n" + "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
		queryRunner.update(create_SQL);
		setFilterTable(TABLE_NAME);
	}

	@AfterClass
	public static void doAfter() throws Exception {
		String drop_SQL = "DROP TABLE IF EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "`";
		queryRunner.update(drop_SQL);
	}

	@Test
	public void yearTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				String[][] testData = { { "2005", "2005" }, { "2015", "2015" }, { "2003", "2003" } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
					      + "`(size_year, unsize_year)VALUES(?,?)";
					queryRunner.update(insert_SQL, testData[i][0], testData[i][1]);
				}
				List<ChangedEvent> events = getEvents(testData.length, false, true, false);
				Assert.assertEquals(testData.length, events.size());
				for (int i = 0; i < testData.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.INSERT, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(3, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testData[i][0],
					      String.valueOf(rowChangedEvent.getColumns().get("size_year").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("size_year").getOldValue());
					Assert.assertEquals(testData[i][0],
					      String.valueOf(rowChangedEvent.getColumns().get("unsize_year").getNewValue()));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsize_year").getOldValue());
				}
			}

		});
	}
}
