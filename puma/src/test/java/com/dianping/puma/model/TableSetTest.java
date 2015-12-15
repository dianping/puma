package com.dianping.puma.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TableSetTest {

	@Test
	public void testAdd() {
		TableSet tableSet = new TableSet();
		List<Table> result;
		List<Table> expected;

		// Add "schema_0.table_0".
		// Before: {}.
		// After: {"schema_0.table_0"}.
		Table table0 = new Table("schema_0", "table_0");
		tableSet.add(table0);
		result = tableSet.listSchemaTables();
		expected = new ArrayList<Table>();
		expected.add(table0);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.table_1".
		// Before: {"schema_0.table_0"}.
		// After: {"schema_0.table_0", "schema_1.table_1"}.
		Table table1 = new Table("schema_1", "table_1");
		tableSet.add(table1);
		result = tableSet.listSchemaTables();
		expected = new ArrayList<Table>();
		expected.add(table0);
		expected.add(table1);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.*".
		// Before: {"schema_0.table_0", "schema_1.table_1"}.
		// After: {"schema_0.table_0", "schema_1.*"}.
		Table table2 = new Table("schema_1", "*");
		tableSet.add(table2);
		result = tableSet.listSchemaTables();
		expected = new ArrayList<Table>();
		expected.add(table0);
		expected.add(table2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));

		// Add "schema_1.table_2".
		// Before: {"schema_0.table_0", "schema_1.*"}.
		// After: {"schema_0.table_0". "schema_1.*"}.
		Table table3 = new Table("schema_1", "table_2");
		tableSet.add(table3);
		result = tableSet.listSchemaTables();
		expected = new ArrayList<Table>();
		expected.add(table0);
		expected.add(table2);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}

	@Test
	public void testGetIncrement() {
		// {"schema_0.table_0", "schema_1.table_1"}
		TableSet tableSet0 = new TableSet();

		Table table0 = new Table("schema_0", "table_0");
		tableSet0.add(table0);

		Table table1 = new Table("schema_1", "table_1");
		tableSet0.add(table1);

		// {"schema_1.*", "schema_2.table_2"}
		TableSet tableSet1 = new TableSet();

		Table table2 = new Table("schema_1", "*");
		tableSet1.add(table2);

		Table table3 = new Table("schema_2", "table_2");
		tableSet1.add(table3);

		TableSet result = tableSet0.getIncrement(tableSet1);
		TableSet expected = new TableSet();
		expected.add(new Table("schema_1", "*"));
		expected.add(new Table("schema_2", "table_2"));

		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}

	@Test
	public void testGetDecrement() {
		// {"schema_0.table_0", "*.table_1"}
		TableSet tableSet0 = new TableSet();
		tableSet0.add(new Table("schema_0", "table_0"));
		tableSet0.add(new Table("*", "table_1"));

		// {"schema_1.table_1", "schema_1.table_2", "schema_0.*"}
		TableSet tableSet1 = new TableSet();
		tableSet1.add(new Table("schema_1", "table_1"));
		tableSet1.add(new Table("schema_1", "table_2"));
		tableSet1.add(new Table("schema_0", "*"));

		TableSet result = tableSet0.getDecrement(tableSet1);
		TableSet expected = new TableSet();
		expected.add(new Table("*", "table_1"));

		Assert.assertTrue(EqualsBuilder.reflectionEquals(result, expected));
	}
}
