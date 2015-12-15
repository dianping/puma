package com.dianping.puma.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SchemaSetTest {

	private SchemaSet schemaSet = new SchemaSet();

	@Before
	public void before() {
		schemaSet.add(new Schema("puma"));
		schemaSet.add(new Schema("test"));
	}

	@Test
	public void testContains() {
		Schema schema0 = new Schema("puma");
		Assert.assertTrue(schemaSet.contains(schema0));

		Schema schema1 = new Schema("hello");
		Assert.assertFalse(schemaSet.contains(schema1));
	}

	@Test
	public void testAdd() {
		schemaSet.add(new Schema("hello"));
		Assert.assertArrayEquals(new Schema[] { new Schema("puma"), new Schema("test"), new Schema("hello") },
				schemaSet.listSchemas().toArray());

		schemaSet.add(new Schema("*"));
		Assert.assertArrayEquals(new Schema[] { new Schema("*") }, schemaSet.listSchemas().toArray());

		schemaSet.add(new Schema("puma"));
		Assert.assertArrayEquals(new Schema[] { new Schema("*") }, schemaSet.listSchemas().toArray());
	}
}
