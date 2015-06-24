package com.dianping.puma.integration.function;

import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class BigIntTypeTest extends AbstractBaseTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(BigIntTypeTest.class);
	
	private static final String TABLE_NAME = "tb_bigInt";
	
	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` (\n"
				+ "`id` int NOT NULL AUTO_INCREMENT, \n" + "`unsigned_bigInt` bigint unsigned DEFAULT NULL, \n"
				+ "`signed_bigInt` bigint DEFAULT NULL, \n" + "`zerofill_bigInt` bigint(2) zerofill DEFAULT NULL, \n"
				+ "`unzerofill_bigInt` bigint(2) DEFAULT NULL, \n" + "PRIMARY KEY (`id`)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
		queryRunner.update(create_SQL);
		setFilterTable(TABLE_NAME);
	}

	@AfterClass
	public static void doAfter() throws Exception {
		String drop_SQL = "DROP TABLE IF EXISTS `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "`";
		queryRunner.update(drop_SQL);
	}

	@Test
	public void bigIntTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				long [][] testData = {{11, 11, 1, 1},{11, -11, 9, 9},{33, 77, 10, 10},{33, -77, 99, 99},{18866, 99987, 100, 100},{18866, -99987, 33333, 33333}};
				for(int i = 0; i < testData.length; i++){
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "`(unsigned_bigInt, signed_bigInt, zerofill_bigInt, unzerofill_bigInt)VALUES(?, ?, ?, ?)";
					queryRunner.update(insert_SQL, testData[i][0], testData[i][1], testData[i][2], testData[i][3]);
				}
				for(int i = 0; i < testData.length; i++){
					List<ChangedEvent> events = getEvents(testData.length, false);
					Assert.assertEquals(testData.length, events.size());
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testData[i][0], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_bigInt").getNewValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_bigInt").getOldValue());
					Assert.assertEquals(testData[i][1], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_bigInt").getNewValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_bigInt").getOldValue());
					Assert.assertEquals(testData[i][2], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_bigInt").getNewValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_bigInt").getOldValue());
					Assert.assertEquals(testData[i][3], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_bigInt").getNewValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_bigInt").getOldValue());
				}
			}

		});	
	}
	
	@Test
	public void bigIntTypeUpdateTest() throws Exception {
		test(new TestLogic() {
			@Override
			public void doLogic() throws Exception {
				long [][] testDataOld = {{11, 11, 1, 1}, {11, -11, 9, 9}, {33, 77, 10, 10}, {33, -77, 99, 99}, {18866, 99987, 100, 100},{18866, -99987, 33333, 33333}};
				long [][] testDataNew = {{11, -11, 1, 1}, {11, -11, 1, 1},{33, -77, 10, 10},{33, -77, 10, 10}, {18866, -99987, 100, 100},{18866, -99987, 100, 100}};
				long [][] testData ={{11, -11, 1, 1, 11}, {33, -77, 10, 10, 33}, {18866, -99987, 100, 100, 18866}};
				for(int i = 0; i < testData.length; i++){
					String update_SQL = "UPDATE `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` SET unsigned_bigInt = ?, signed_bigInt = ?, zerofill_bigInt = ?, unzerofill_bigInt = ? WHERE unsigned_bigInt = ?";
					queryRunner.update(update_SQL, testData[i][0], testData[i][1], testData[i][2], testData[i][3], testData[i][4]);
				}
				for(int i = 0; i < testDataOld.length; i++){
					List<ChangedEvent> events = getEvents(testDataOld.length, false);
					Assert.assertEquals(testDataOld.length, events.size());
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testDataNew[i][0], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_bigInt").getNewValue())).intValue());
					Assert.assertEquals(testDataOld[i][0], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_bigInt").getOldValue())).intValue());
					Assert.assertEquals(testDataNew[i][1], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_bigInt").getNewValue())).intValue());
					Assert.assertEquals(testDataOld[i][1], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_bigInt").getOldValue())).intValue());
					Assert.assertEquals(testDataNew[i][2], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_bigInt").getNewValue())).intValue());
					Assert.assertEquals(testDataOld[i][2], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_bigInt").getOldValue())).intValue());
					Assert.assertEquals(testDataNew[i][3], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_bigInt").getNewValue())).intValue());
					Assert.assertEquals(testDataOld[i][3], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_bigInt").getOldValue())).intValue());
				}
			}

		});	
	}
	
	@Test
	public void bigIntTypeDeleteTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				long [][] testDataOld = {{11, -11, 1, 1}, {11, -11, 1, 1},{33, -77, 10, 10},{33, -77, 10, 10}, {18866, -99987, 100, 100},{18866, -99987, 100, 100}};
				long [][] testData = {{11},{33},{18866}};
				for(int i = 0; i < testData.length; i++){
					String delete_SQL = "DELETE FROM `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` WHERE unsigned_bigInt = ?";
					queryRunner.update(delete_SQL, testData[i][0]);
				}
				for(int i = 0; i < testDataOld.length; i++){
					List<ChangedEvent> events = getEvents(testDataOld.length, false);
					Assert.assertEquals(testDataOld.length, events.size());
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(5, rowChangedEvent.getColumns().size());
					Assert.assertEquals(testDataOld[i][0], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_bigInt").getOldValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_bigInt").getNewValue());
					Assert.assertEquals(testDataOld[i][1], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_bigInt").getOldValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_bigInt").getNewValue());
					Assert.assertEquals(testDataOld[i][2], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_bigInt").getOldValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_bigInt").getNewValue());
					Assert.assertEquals(testDataOld[i][3], Long.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_bigInt").getOldValue())).intValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_bigInt").getNewValue());
				}
			}

		});	
	}

}