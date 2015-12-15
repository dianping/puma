package com.dianping.puma.model;

import org.junit.Assert;
import org.junit.Test;

public class TableTest {

	@Test
	public void testContains() {
		Table table1, table2;
		boolean result;

		table1 = new Table("schema", "table");
		table2 = new Table("schema", "table");
		result = table1.contains(table2);
		Assert.assertTrue(result);

		table1 = new Table("*", "table");
		table2 = new Table("schema", "table");
		result = table1.contains(table2);
		Assert.assertTrue(result);

		table1 = new Table("schema", "*");
		table2 = new Table("schema", "table");
		result = table1.contains(table2);
		Assert.assertTrue(result);

		table1 = new Table("schema", "*");
		table2 = new Table("*", "table");
		result = table1.contains(table2);
		Assert.assertFalse(result);

		table1 = new Table("*", "table");
		table2 = new Table("schema", "*");
		result = table1.contains(table2);
		Assert.assertFalse(result);
	}
}
