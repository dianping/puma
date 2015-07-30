package com.dianping.puma.syncserver.util;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.util.sql.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SqlParserTest {

	@Test
	public void testParseInsert() throws Exception {
		RowChangedEvent rowInsert = new RowChangedEvent();
		rowInsert.setDmlType(DMLType.INSERT);
		rowInsert.setTransactionBegin(false);
		rowInsert.setTransactionCommit(false);
		rowInsert.setDatabase("insert-test-database");
		rowInsert.setTable("insert-test-table");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new LinkedHashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap.put("pk0", new RowChangedEvent.ColumnInfo(true, null, 0));
		columnInfoMap.put("pk1", new RowChangedEvent.ColumnInfo(true, null, "a"));
		columnInfoMap.put("npk0", new RowChangedEvent.ColumnInfo(false, null, 1));
		columnInfoMap.put("npk1", new RowChangedEvent.ColumnInfo(false, null, "b"));
		rowInsert.setColumns(columnInfoMap);

		String expectedSql = "INSERT INTO `insert-test-database`.`insert-test-table` (`pk0`, `pk1`, `npk0`, `npk1`) VALUES (?,?,?,?)";
		String resultSql = SqlParser.parseSql(rowInsert);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));

		Object[] expectedArgs = new Object[] {0, "a", 1, "b"};
		Object[] resultArgs = SqlParser.parseArgs(rowInsert);
		assertArrayEquals(expectedArgs, resultArgs);
	}

	@Test
	public void testParseDelete() throws Exception {
		RowChangedEvent rowDelete = new RowChangedEvent();
		rowDelete.setDmlType(DMLType.DELETE);
		rowDelete.setTransactionBegin(false);
		rowDelete.setTransactionCommit(false);
		rowDelete.setDatabase("delete-test-database");
		rowDelete.setTable("delete-test-table");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new LinkedHashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap.put("npk0", new RowChangedEvent.ColumnInfo(false, 1, null));
		columnInfoMap.put("pk0", new RowChangedEvent.ColumnInfo(true, 0, null));
		columnInfoMap.put("pk1", new RowChangedEvent.ColumnInfo(true, "a", null));
		columnInfoMap.put("npk1", new RowChangedEvent.ColumnInfo(false, "b", null));
		rowDelete.setColumns(columnInfoMap);

		String expectedSql = "DELETE FROM `delete-test-database`.`delete-test-table` WHERE `pk0`=? AND `pk1`=?";
		String resultSql = SqlParser.parseSql(rowDelete);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));

		Object[] expectedArgs = new Object[] {0, "a"};
		Object[] resultArgs = SqlParser.parseArgs(rowDelete);
		assertArrayEquals(expectedArgs, resultArgs);
	}

	@Test
	public void testParseUpdate() throws Exception {
		RowChangedEvent rowUpdate = new RowChangedEvent();
		rowUpdate.setDmlType(DMLType.UPDATE);
		rowUpdate.setTransactionBegin(false);
		rowUpdate.setTransactionCommit(false);
		rowUpdate.setDatabase("update-test-database");
		rowUpdate.setTable("update-test-table");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new LinkedHashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap.put("npk0", new RowChangedEvent.ColumnInfo(false, 1, 10));
		columnInfoMap.put("pk0", new RowChangedEvent.ColumnInfo(true, 0, 0));
		columnInfoMap.put("pk1", new RowChangedEvent.ColumnInfo(true, "a", "a"));
		columnInfoMap.put("npk1", new RowChangedEvent.ColumnInfo(false, "b", "bb"));
		rowUpdate.setColumns(columnInfoMap);

		String expectedSql = "UPDATE `update-test-database`.`update-test-table` SET `npk0`=?, `pk0`=?, `pk1`=?, `npk1`=? WHERE `pk0`=? AND `pk1`=?";
		String resultSql = SqlParser.parseSql(rowUpdate);
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(resultSql));

		Object[] expectedArgs = new Object[] {10, 0, "a", "bb", 0, "a"};
		Object[] resultArgs = SqlParser.parseArgs(rowUpdate);
		assertArrayEquals(expectedArgs, resultArgs);
	}
}