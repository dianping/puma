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
import com.dianping.puma.core.util.ByteArrayUtils;

/***
 * bit type test
 * 
 * @author qi.yin
 *
 */
public class BitTypeTest extends AbstractBaseTest {

	private static final Logger LOG = LoggerFactory.getLogger(BitTypeTest.class);

	private static final String TABLE_NAME = "tb_bit";

	@BeforeClass
	public static void doBefore() throws Exception {
		String create_SQL = "CREATE TABLE IF NOT EXISTS `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "` (\n"
				+ "`id` int NOT NULL AUTO_INCREMENT, \n" + "`default_bit` bit(3) DEFAULT NULL, \n"
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
	public void bitTypeInsertTest() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				byte[][] testData = { { 6 }, { 5 }, { 4 } };
				for (int i = 0; i < testData.length; i++) {
					String insert_SQL = "INSERT INTO `" + SCHEMA_NAME + "`.`" + TABLE_NAME + "`(default_bit)VALUES(?)";
					queryRunner.update(insert_SQL, testData[i][0]);
				}
				List<ChangedEvent> events = getEvents(testData.length, false, true, false);
				Assert.assertEquals(testData.length, events.size());
				for (int i = 0; i < testData.length; i++) {
					Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
					RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
					Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
					Assert.assertEquals(TABLE_NAME, rowChangedEvent.getTable());
					Assert.assertEquals(SCHEMA_NAME, rowChangedEvent.getDatabase());
					Assert.assertEquals(2, rowChangedEvent.getColumns().size());
					byte[] value = (byte[]) rowChangedEvent.getColumns().get("default_bit").getNewValue();
					Assert.assertEquals(testData[i][0], ByteArrayUtils.byteArrayToInt(value, 0, value.length));
					Assert.assertEquals(null, rowChangedEvent.getColumns().get("default_bit").getOldValue());
				}
			}

		});
	}
}
