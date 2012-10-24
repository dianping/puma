/**
 * Project: puma-core
 * 
 * File Created at 2012-7-27
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO Comment of ByteArrayUtilsTest
 * 
 * @author Leo Liang
 * 
 */
public class ByteArrayUtilsTest {
	@Test
	public void testByteArrayToInt() {
		byte[] bytes = new byte[] { 0, 0, 0, 4 };
		Assert.assertEquals(4, ByteArrayUtils.byteArrayToInt(bytes, 0, 4));
	}

	@Test
	public void testByteArrayToInt2() {
		byte[] bytes = new byte[] { 1, 0, 0, 4 };
		Assert.assertEquals(16777220, ByteArrayUtils.byteArrayToInt(bytes, 0, 4));
	}

	@Test
	public void testByteArrayToInt3() {
		byte[] bytes = new byte[] { 1, 2, 0, 4 };
		Assert.assertEquals(16908292, ByteArrayUtils.byteArrayToInt(bytes, 0, 4));
	}

	@Test
	public void testByteArrayToInt4() {
		byte[] bytes = new byte[] { 1, 2, 1, 4 };
		Assert.assertEquals(16908548, ByteArrayUtils.byteArrayToInt(bytes, 0, 4));
	}

	@Test
	public void testIntToByteArray() {
		Assert.assertArrayEquals(new byte[] { 0, 0, 0, 4 }, ByteArrayUtils.intToByteArray(4));
	}

	@Test
	public void testIntToByteArray2() {
		Assert.assertArrayEquals(new byte[] { 1, 0, 0, 4 }, ByteArrayUtils.intToByteArray(16777220));
	}

	@Test
	public void testIntToByteArray3() {
		Assert.assertArrayEquals(new byte[] { 1, 2, 0, 4 }, ByteArrayUtils.intToByteArray(16908292));
	}

	@Test
	public void testIntToByteArray4() {
		Assert.assertArrayEquals(new byte[] { 1, 2, 1, 4 }, ByteArrayUtils.intToByteArray(16908548));
	}
}
