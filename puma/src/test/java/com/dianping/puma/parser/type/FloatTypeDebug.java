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
 * float type test
 * 
 * @author qi.yin
 *
 */
public class FloatTypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_float";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
		      + "`id` int NOT NULL AUTO_INCREMENT, \n" + "`unsigned_float` float unsigned DEFAULT NULL, \n"
		      + "`signed_float` float DEFAULT NULL, \n" + "`zerofill_float` float(10,2) zerofill DEFAULT NULL, \n"
		      + "`unzerofill_float` float(10,2) DEFAULT NULL, \n" + "PRIMARY KEY (`id`)"
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
	public void floatTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				float[][] testData = { { (float) 11.333, (float) 11.222, (float) 1.11, (float) 1.11 },
				      { (float) 11.333, (float) -11.222, (float) 9.33, (float) 9.33 },
				      { (float) 33.234, (float) 77.256, (float) 10.34, (float) 10.34 },
				      { (float) 33.234, (float) -77.256, (float) 99.58, (float) 99.58 },
				      { (float) 18866.153, (float) 99987.347, (float) 100.1, 100 },
				      { (float) 18866.153, (float) -99987.347, (float) 33333.2, (float) 33333.55 } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
					      + "`(unsigned_float, signed_float, zerofill_float, unzerofill_float)VALUES(?, ?, ?, ?)";
					queryRunner.update(insert_SQL, testData[i][0], testData[i][1], testData[i][2], testData[i][3]);
				}
				List<ChangedEvent> events = getEvents(testData.length, false, true, false);
				Assert.assertEquals(testData.length, events.size());
				for (int i = 0; i < testData.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.INSERT, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testData[i][0],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_float").getOldValue());
					Assert.assertEquals(testData[i][1],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_float").getOldValue());
					Assert.assertEquals(testData[i][2],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_float").getOldValue());
					Assert.assertEquals(testData[i][3],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_float").getOldValue());
				}
			}

		});
	}

	@Test
	public void floatTypeUpdateTest() throws Exception {
		test(new TestLogic() {
			@Override
			public void doLogic() throws Exception {
				float[][] testDataOld = { { (float) 11.333, (float) 11.222, (float) 1.11, (float) 1.11 },
				      { (float) 11.333, (float) -11.222, (float) 9.33, (float) 9.33 },
				      { (float) 33.234, (float) 77.256, (float) 10.34, (float) 10.34 },
				      { (float) 33.234, (float) -77.256, (float) 99.58, (float) 99.58 },
				      { (float) 18866.153, (float) 99987.347, (float) 100.1, 100 },
				      { (float) 18866.153, (float) -99987.347, (float) 33333.2, (float) 33333.55 } };
				float[][] testDataNew = { { (float) 11.333, (float) -11.222, (float) 1.11, (float) 1.11 },
				      { (float) 11.333, (float) -11.222, (float) 1.11, (float) 1.11 },
				      { (float) 33.234, (float) -77.256, (float) 10.34, (float) 10.34 },
				      { (float) 33.234, (float) -77.256, (float) 10.34, (float) 10.34 },
				      { (float) 18866.153, (float) -99987.347, (float) 100.1, 100 },
				      { (float) 18866.153, (float) -99987.347, (float) 100.1, 100 } };
				int[] whereData = { 1, 2, 3, 4, 5, 6 };
				for (int i = 0; i < testDataNew.length; i++) {
					String update_SQL = "UPDATE `"
					      + SCHEMA_NAME
					      + "`.`"
					      + TABLE_NAME
					      + "` SET unsigned_float = ?, signed_float = ?, zerofill_float = ?, unzerofill_float = ? WHERE id = ?";
					queryRunner.update(update_SQL, testDataNew[i][0], testDataNew[i][1], testDataNew[i][2],
					      testDataNew[i][3], whereData[i]);
				}
				List<ChangedEvent> events = getEvents(testDataOld.length, false, true, false);
				Assert.assertEquals(testDataOld.length, events.size());
				for (int i = 0; i < testDataOld.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.UPDATE, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testDataNew[i][0],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(testDataOld[i][0],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(testDataNew[i][1],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(testDataOld[i][1],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(testDataNew[i][2],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(testDataOld[i][2],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(testDataNew[i][3],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_float").getNewValue()))
					            .floatValue());
					Assert.assertEquals(testDataOld[i][3],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_float").getOldValue()))
					            .floatValue());
				}
			}

		});
	}

	@Test
	public void floatTypeDeleteTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				float[][] testDataOld = { { (float) 11.333, (float) -11.222, (float) 1.11, (float) 1.11 },
				      { (float) 11.333, (float) -11.222, (float) 1.11, (float) 1.11 },
				      { (float) 33.234, (float) -77.256, (float) 10.34, (float) 10.34 },
				      { (float) 33.234, (float) -77.256, (float) 10.34, (float) 10.34 },
				      { (float) 18866.153, (float) -99987.347, (float) 100.1, 100 },
				      { (float) 18866.153, (float) -99987.347, (float) 100.1, 100 } };
				int[] whereData = { 1, 2, 3, 4, 5, 6 };
				for (int i = 0; i < whereData.length; i++) {
					String delete_SQL = "DELETE FROM `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` WHERE id = ?";
					queryRunner.update(delete_SQL, whereData[i]);
				}
				List<ChangedEvent> events = getEvents(testDataOld.length, false, true, false);
				Assert.assertEquals(testDataOld.length, events.size());
				for (int i = 0; i < testDataOld.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(DMLType.DELETE, rowChangedEvent.getDmlType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testDataOld[i][0],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_float").getNewValue());
					Assert.assertEquals(testDataOld[i][1],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_float").getNewValue());
					Assert.assertEquals(testDataOld[i][2],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_float").getNewValue());
					Assert.assertEquals(testDataOld[i][3],
					      Float.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_float").getOldValue()))
					            .floatValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_float").getNewValue());
				}
			}

		});
	}
}
