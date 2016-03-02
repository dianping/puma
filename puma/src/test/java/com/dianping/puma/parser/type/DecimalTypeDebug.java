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
 * decimal type test
 * 
 * @author qi.yin
 *
 */
public class DecimalTypeDebug extends AbstractBaseDebug {

	private static final String TABLE_NAME = "tb_decimal";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
		      + "`id` int NOT NULL AUTO_INCREMENT, \n" + "`unsigned_decimal` decimal(10,3) unsigned DEFAULT NULL, \n"
		      + "`signed_decimal` decimal(10,3) DEFAULT NULL, \n"
		      + "`zerofill_decimal` decimal(10,2) zerofill DEFAULT NULL, \n"
		      + "`unzerofill_decimal` decimal(10,2) DEFAULT NULL, \n" + "PRIMARY KEY (`id`)"
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
	public void decimalTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				double[][] testData = { { 11.333, 11.222, 1.11, 1.11 }, { 11.333, -11.222, 9.33, 9.33 },
				      { 33.234, 77.256, 10.34, 10.34 }, { 33.234, -77.256, 99.58, 99.58 },
				      { 18866.153, 99987.347, 100.1, 100 }, { 18866.153, -99987.347, 33333.2, 33333.55 } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME
					      + "`(unsigned_decimal, signed_decimal, zerofill_decimal, unzerofill_decimal)VALUES(?, ?, ?, ?)";
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
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_decimal").getOldValue());
					Assert.assertEquals(testData[i][1],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_decimal").getOldValue());
					Assert.assertEquals(testData[i][2],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_decimal").getOldValue());
					Assert.assertEquals(
					      testData[i][3],
					      Double.valueOf(
					            String.valueOf(rowChangedEvent.getColumns().get("unzerofill_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_decimal").getOldValue());
				}
			}

		});
	}

	@Test
	public void decimalTypeUpdateTest() throws Exception {
		test(new TestLogic() {
			@Override
			public void doLogic() throws Exception {
				double[][] testDataOld = { { 11.333, 11.222, 1.11, 1.11 }, { 11.333, -11.222, 9.33, 9.33 },
				      { 33.234, 77.256, 10.34, 10.34 }, { 33.234, -77.256, 99.58, 99.58 },
				      { 18866.153, 99987.347, 100.1, 100 }, { 18866.153, -99987.347, 33333.2, 33333.55 } };
				double[][] testDataNew = { { 11.333, -11.222, 1.11, 1.11 }, { 11.333, -11.222, 1.11, 1.11 },
				      { 33.234, -77.256, 10.34, 10.34 }, { 33.234, -77.256, 10.34, 10.34 },
				      { 18866.153, -99987.347, 100.1, 100 }, { 18866.153, -99987.347, 100.1, 100 } };
				int[] whereData = { 1, 2, 3, 4, 5, 6 };
				for (int i = 0; i < testDataNew.length; i++) {
					String update_SQL = "UPDATE `"
					      + SCHEMA_NAME
					      + "`.`"
					      + TABLE_NAME
					      + "` SET unsigned_decimal = ?, signed_decimal = ?, zerofill_decimal = ?, unzerofill_decimal = ? WHERE id = ?";
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
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(testDataOld[i][0],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(testDataNew[i][1],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(testDataOld[i][1],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(testDataNew[i][2],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(testDataOld[i][2],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(
					      testDataNew[i][3],
					      Double.valueOf(
					            String.valueOf(rowChangedEvent.getColumns().get("unzerofill_decimal").getNewValue()))
					            .doubleValue());
					Assert.assertEquals(
					      testDataOld[i][3],
					      Double.valueOf(
					            String.valueOf(rowChangedEvent.getColumns().get("unzerofill_decimal").getOldValue()))
					            .doubleValue());
				}
			}

		});
	}

	@Test
	public void decimalTypeDeleteTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				double[][] testDataOld = { { 11.333, -11.222, 1.11, 1.11 }, { 11.333, -11.222, 1.11, 1.11 },
				      { 33.234, -77.256, 10.34, 10.34 }, { 33.234, -77.256, 10.34, 10.34 },
				      { 18866.153, -99987.347, 100.1, 100 }, { 18866.153, -99987.347, 100.1, 100 } };
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
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_decimal").getNewValue());
					Assert.assertEquals(testDataOld[i][1],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_decimal").getNewValue());
					Assert.assertEquals(testDataOld[i][2],
					      Double.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_decimal").getNewValue());
					Assert.assertEquals(
					      testDataOld[i][3],
					      Double.valueOf(
					            String.valueOf(rowChangedEvent.getColumns().get("unzerofill_decimal").getOldValue()))
					            .doubleValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_decimal").getNewValue());
				}
			}

		});
	}
}
