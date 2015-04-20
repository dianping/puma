package com.dianping.puma.core.model;

import org.junit.Assert;
import org.junit.Test;

public class SchemaTableTest {

	@Test
	public void testContains() {
		SchemaTable schemaTable1, schemaTable2;
		boolean result;

		schemaTable1 = new SchemaTable("schema", "table");
		schemaTable2 = new SchemaTable("schema", "table");
		result = schemaTable1.contains(schemaTable2);
		Assert.assertTrue(result);

		schemaTable1 = new SchemaTable("*", "table");
		schemaTable2 = new SchemaTable("schema", "table");
		result = schemaTable1.contains(schemaTable2);
		Assert.assertTrue(result);

		schemaTable1 = new SchemaTable("schema", "*");
		schemaTable2 = new SchemaTable("schema", "table");
		result = schemaTable1.contains(schemaTable2);
		Assert.assertTrue(result);

		schemaTable1 = new SchemaTable("schema", "*");
		schemaTable2 = new SchemaTable("*", "table");
		result = schemaTable1.contains(schemaTable2);
		Assert.assertFalse(result);

		schemaTable1 = new SchemaTable("*", "table");
		schemaTable2 = new SchemaTable("schema", "*");
		result = schemaTable1.contains(schemaTable2);
		Assert.assertFalse(result);
	}
}
