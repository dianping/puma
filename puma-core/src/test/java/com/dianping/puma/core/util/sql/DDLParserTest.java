package com.dianping.puma.core.util.sql;

import org.junit.Test;

public class DDLParserTest {

	@Test
	public void parseTest() {
		DDLParser.parse("CREATE TABLE", "hello");
	}
}
