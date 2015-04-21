package com.dianping.puma.core.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SchemaTableSetTest {

	@Test
	public void testAdd() {
		SchemaTableSet schemaTableSet = new SchemaTableSet();
		List<SchemaTable> result;
		List<SchemaTable> expected;

		// Add "schema_0.table_0".
		// Before: {}.
		// After: {"schema_0.table_0"}.
		SchemaTable schemaTable0 = new SchemaTable("schema_0", "table_0");
		schemaTableSet.add(schemaTable0);
		result = schemaTableSet.listSchemaTables();
		expected = new ArrayList<SchemaTable>();
		expected.add(schemaTable0);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.table_1".
		// Before: {"schema_0.table_0"}.
		// After: {"schema_0.table_0", "schema_1.table_1"}.
		SchemaTable schemaTable1 = new SchemaTable("schema_1", "table_1");
		schemaTableSet.add(schemaTable1);
		result = schemaTableSet.listSchemaTables();
		expected = new ArrayList<SchemaTable>();
		expected.add(schemaTable0);
		expected.add(schemaTable1);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.*".
		// Before: {"schema_0.table_0", "schema_1.table_1"}.
		// After: {"schema_0.table_0", "schema_1.*"}.
		SchemaTable schemaTable2 = new SchemaTable("schema_1", "*");
		schemaTableSet.add(schemaTable2);
		result = schemaTableSet.listSchemaTables();
		expected = new ArrayList<SchemaTable>();
		expected.add(schemaTable0);
		expected.add(schemaTable2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.table_2".
		// Before: {"schema_0.table_0", "schema_1.*"}.
		// After: {"schema_0.table_0". "schema_1.*"}.
		SchemaTable schemaTable3 = new SchemaTable("schema_1", "table_2");
		schemaTableSet.add(schemaTable3);
		result = schemaTableSet.listSchemaTables();
		expected = new ArrayList<SchemaTable>();
		expected.add(schemaTable0);
		expected.add(schemaTable2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}

	@Test
	public void testGetIncrement() {
		// {"schema_0.table_0", "schema_1.table_1"}
		SchemaTableSet schemaTableSet0 = new SchemaTableSet();

		SchemaTable schemaTable0 = new SchemaTable("schema_0", "table_0");
		schemaTableSet0.add(schemaTable0);

		SchemaTable schemaTable1 = new SchemaTable("schema_1", "table_1");
		schemaTableSet0.add(schemaTable1);

		// {"schema_1.*", "schema_2.table_2"}
		SchemaTableSet schemaTableSet1 = new SchemaTableSet();

		SchemaTable schemaTable2 = new SchemaTable("schema_1", "*");
		schemaTableSet1.add(schemaTable2);

		SchemaTable schemaTable3 = new SchemaTable("schema_2", "table_2");
		schemaTableSet1.add(schemaTable3);

		SchemaTableSet result = schemaTableSet0.getIncrement(schemaTableSet1);
		SchemaTableSet expected = new SchemaTableSet();
		expected.add(new SchemaTable("schema_1", "*"));
		expected.add(new SchemaTable("schema_2", "table_2"));

		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}

	@Test
	public void testGetDecrement() {
		// {"schema_0.table_0", "*.table_1"}
		SchemaTableSet schemaTableSet0 = new SchemaTableSet();
		schemaTableSet0.add(new SchemaTable("schema_0", "table_0"));
		schemaTableSet0.add(new SchemaTable("*", "table_1"));

		// {"schema_1.table_1", "schema_1.table_2", "schema_0.*"}
		SchemaTableSet schemaTableSet1 = new SchemaTableSet();
		schemaTableSet1.add(new SchemaTable("schema_1", "table_1"));
		schemaTableSet1.add(new SchemaTable("schema_1", "table_2"));
		schemaTableSet1.add(new SchemaTable("schema_0", "*"));

		SchemaTableSet result = schemaTableSet0.getDecrement(schemaTableSet1);
		SchemaTableSet expected = new SchemaTableSet();
		expected.add(new SchemaTable("*", "table_1"));

		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}
}
