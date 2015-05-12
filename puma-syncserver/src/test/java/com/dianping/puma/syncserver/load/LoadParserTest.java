package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.LoadParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoadParserTest {

	@Test
	public void testParseSql() {
		RowChangedEvent event0 = new RowChangedEvent();
		event0.setDatabase("puma");
		event0.setTable("test");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new HashMap <String, RowChangedEvent.ColumnInfo>();
		RowChangedEvent.ColumnInfo id = new RowChangedEvent.ColumnInfo(true, "0", "1");
		columnInfoMap.put("id", id);
		RowChangedEvent.ColumnInfo name = new RowChangedEvent.ColumnInfo(false, "Peter", "Linda");
		columnInfoMap.put("name", name);
		event0.setColumns(columnInfoMap);
		event0.setDmlType(DMLType.INSERT);

		String sql0 = LoadParser.parseSql(event0);
		System.out.println(sql0);

		String sql1 = LoadParser.parseSql(event0);
		System.out.println(sql1);
	}

	@Test
	public void testParseArgs() {
		RowChangedEvent event0 = new RowChangedEvent();
		event0.setDatabase("puma");
		event0.setTable("test");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = new HashMap <String, RowChangedEvent.ColumnInfo>();
		RowChangedEvent.ColumnInfo id = new RowChangedEvent.ColumnInfo(true, "0", "1");
		columnInfoMap.put("id", id);
		RowChangedEvent.ColumnInfo name = new RowChangedEvent.ColumnInfo(false, "Peter", "Linda");
		columnInfoMap.put("name", name);
		event0.setColumns(columnInfoMap);
		event0.setDmlType(DMLType.DELETE);

		Object[] args0 = LoadParser.parseArgs(event0);
		System.out.println(Arrays.toString(args0));
	}
}
