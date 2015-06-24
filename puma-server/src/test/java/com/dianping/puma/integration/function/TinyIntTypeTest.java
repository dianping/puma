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

public class TinyIntTypeTest extends AbstractBaseTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(TinyIntTypeTest.class);
	
	private static final String TABLE_NAME = "tb_tinyInt";
	
	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` (\n"
				+ "`id` int NOT NULL AUTO_INCREMENT, \n" + "`unsigned_tinyInt` tinyint unsigned DEFAULT NULL, \n"
				+ "`signed_tinyInt` tinyint DEFAULT NULL, \n" + "`zerofill_tinyInt` tinyint(2) zerofill DEFAULT NULL, \n"
				+ "`unzerofill_tinyInt` tinyint(2) DEFAULT NULL, \n" + "PRIMARY KEY (`id`)"
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
	public void tinyIntTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				byte [][] testData = {{11, 11, 1, 1},{11, -11, 9, 9},{33, 77, 10, 10},{33, -77, 99, 99},{126, 127, 100, 100},{126, -127, 127, 127}};
				for(int i = 0; i < testData.length; i++){
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "`(unsigned_tinyInt, signed_tinyInt, zerofill_tinyInt, unzerofill_tinyInt)VALUES(?, ?, ?, ?)";
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
					Assert.assertEquals(testData[i][0], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_tinyInt").getOldValue());
					Assert.assertEquals(testData[i][1], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_tinyInt").getOldValue());
					Assert.assertEquals(testData[i][2], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_tinyInt").getOldValue());
					Assert.assertEquals(testData[i][3], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_tinyInt").getOldValue());
				}
			}

		});	
	}
	
	@Test
	public void mediumIntTypeUpdateTest() throws Exception {
		test(new TestLogic() {
			@Override
			public void doLogic() throws Exception {
				byte [][] testDataOld = {{11, 11, 1, 1}, {11, -11, 9, 9}, {33, 77, 10, 10}, {33, -77, 99, 99}, {126, 127, 100, 100},{126, -127, 127, 127}};
				byte [][] testDataNew = {{11, -11, 1, 1}, {11, -11, 1, 1},{33, -77, 10, 10},{33, -77, 10, 10}, {126, -127, 100, 100},{126, -127, 100, 100}};
				byte [][] testData ={{11, -11, 1, 1, 11}, {33, -77, 10, 10, 33}, {126, -127, 100, 100, 126}};
				for(int i = 0; i < testData.length; i++){
					String update_SQL = "UPDATE `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` SET unsigned_tinyInt = ?, signed_tinyInt = ?, zerofill_tinyInt = ?, unzerofill_tinyInt = ? WHERE unsigned_tinyInt = ?";
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
					Assert.assertEquals(testDataNew[i][0], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(testDataOld[i][0], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(testDataNew[i][1], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(testDataOld[i][1], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(testDataNew[i][2], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(testDataOld[i][2], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(testDataNew[i][3], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_tinyInt").getNewValue())).byteValue());
					Assert.assertEquals(testDataOld[i][3], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_tinyInt").getOldValue())).byteValue());
				}
			}

		});	
	}
	
	@Test
	public void mediumIntTypeDeleteTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				byte [][] testDataOld = {{11, -11, 1, 1}, {11, -11, 1, 1},{33, -77, 10, 10},{33, -77, 10, 10}, {126, -127, 100, 100},{126, -127, 100, 100}};
				byte [][] testData = {{11},{33},{126}};
				for(int i = 0; i < testData.length; i++){
					String delete_SQL = "DELETE FROM `" + SCHEMA_NAME +"`.`" + TABLE_NAME + "` WHERE unsigned_tinyInt = ?";
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
					Assert.assertEquals(testDataOld[i][0], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unsigned_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unsigned_tinyInt").getNewValue());
					Assert.assertEquals(testDataOld[i][1], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("signed_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("signed_tinyInt").getNewValue());
					Assert.assertEquals(testDataOld[i][2], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("zerofill_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("zerofill_tinyInt").getNewValue());
					Assert.assertEquals(testDataOld[i][3], Byte.valueOf(String.valueOf(rowChangedEvent.getColumns().get("unzerofill_tinyInt").getOldValue())).byteValue());
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("unzerofill_tinyInt").getNewValue());
				}
			}

		});	
	}

}
