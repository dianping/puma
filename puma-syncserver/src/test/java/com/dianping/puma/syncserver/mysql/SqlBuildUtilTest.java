package com.dianping.puma.syncserver.mysql;

import com.dianping.puma.core.event.RowChangedEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SqlBuildUtilTest {

	@Test
	public void testBuildReplaceSql() {
		RowChangedEvent rowChangedEvent = new RowChangedEvent();
		rowChangedEvent.setDatabase("puma");
		rowChangedEvent.setTable("puma_table");
		rowChangedEvent.setTransactionBegin(false);
		rowChangedEvent.setTransactionCommit(false);
		Map<String, RowChangedEvent.ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
		RowChangedEvent.ColumnInfo columnInfo0 = new RowChangedEvent.ColumnInfo(false, 1, 2);
		RowChangedEvent.ColumnInfo columnInfo1 = new RowChangedEvent.ColumnInfo(true, "a", "b");
		columns.put("age", columnInfo0);
		columns.put("name", columnInfo1);
		rowChangedEvent.setColumns(columns);

		String sql = SqlBuildUtil.buildReplaceSql(rowChangedEvent);
		System.out.println(sql);
	}
}
