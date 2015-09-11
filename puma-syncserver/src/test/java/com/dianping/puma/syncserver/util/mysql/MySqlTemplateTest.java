package com.dianping.puma.syncserver.util.mysql;

import com.dianping.puma.syncserver.common.binlog.Column;
import com.dianping.puma.syncserver.common.binlog.DeleteEvent;
import com.dianping.puma.syncserver.common.binlog.InsertEvent;
import com.dianping.puma.syncserver.common.binlog.UpdateEvent;
import com.dianping.puma.syncserver.util.mysql.MySqlTemplate;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class MySqlTemplateTest {

	@Test
	public void testRenderInsert() throws Exception {
		InsertEvent InsertEvent = new InsertEvent();
		InsertEvent.setDatabase("insert-test-database");
		InsertEvent.setTable("insert-test-table");
		InsertEvent.addColumn("pk0", new Column(true, null, 0));
		InsertEvent.addColumn("pk1", new Column(true, null, "a"));
		InsertEvent.addColumn("npk0", new Column(false, null, 1));
		InsertEvent.addColumn("npk1", new Column(false, null, "b"));

		String expectedSql = "INSERT INTO `insert-test-database`.`insert-test-table` (`pk0`, `pk1`, `npk0`, `npk1`) VALUES (?,?,?,?)";
		String resultSql = MySqlTemplate.render(
				InsertEvent.getDatabase(),
				InsertEvent.getTable(),
				InsertEvent.getColumns(),
				MySqlTemplate.RenderTemplate.INSERT);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));
	}

	@Test
	public void testRenderDelete() throws Exception {
		DeleteEvent deleteEvent = new DeleteEvent();
		deleteEvent.setDatabase("delete-test-database");
		deleteEvent.setTable("delete-test-table");
		deleteEvent.addColumn("npk0", new Column(false, 1, null));
		deleteEvent.addColumn("pk0", new Column(true, 0, null));
		deleteEvent.addColumn("pk1", new Column(true, "a", null));
		deleteEvent.addColumn("npk1", new Column(false, "b", null));

		String expectedSql = "DELETE FROM `delete-test-database`.`delete-test-table` WHERE `pk0`=? AND `pk1`=?";
		String resultSql = MySqlTemplate.render(
				deleteEvent.getDatabase(),
				deleteEvent.getTable(),
				deleteEvent.getColumns(),
				MySqlTemplate.RenderTemplate.DELETE);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));
	}

	@Test
	public void testRenderUpdate() throws Exception {
		UpdateEvent updateEvent = new UpdateEvent();
		updateEvent.setDatabase("update-test-database");
		updateEvent.setTable("update-test-table");
		updateEvent.addColumn("npk0", new Column(false, 1, 10));
		updateEvent.addColumn("pk0", new Column(true, 0, 0));
		updateEvent.addColumn("pk1", new Column(true, "a", "a"));
		updateEvent.addColumn("npk1", new Column(false, "b", "bb"));

		String expectedSql = "UPDATE `update-test-database`.`update-test-table` SET `npk0`=?, `pk0`=?, `pk1`=?, `npk1`=? WHERE `pk0`=? AND `pk1`=?";
		String resultSql = MySqlTemplate.render(
				updateEvent.getDatabase(),
				updateEvent.getTable(),
				updateEvent.getColumns(),
				MySqlTemplate.RenderTemplate.UPDATE);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));
	}
}