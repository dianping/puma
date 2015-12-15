package com.dianping.puma.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SchemaTest {

	@Before
	public void before() {
	}

	@Test
	public void testContains() {
		// Case 1.
		Schema schema00 = new Schema("puma");
		Schema schema01 = new Schema("puma");
		Assert.assertTrue(schema00.contains(schema01));

		// Case 2.
		Schema schema10 = new Schema("puma");
		Schema schema11 = new Schema("test");
		Assert.assertFalse(schema10.contains(schema11));

		// Case 3.
		Schema schema20 = new Schema("*");
		Schema schema21 = new Schema("puma");
		Assert.assertTrue(schema20.contains(schema21));

		// Case 4.
		Schema schema30 = new Schema("*");
		Schema schema31 = new Schema(null);
		Assert.assertFalse(schema30.contains(schema31));
	}
}
