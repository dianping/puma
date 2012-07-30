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

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO Comment of StreamUtilsTest
 * 
 * @author Leo Liang
 * 
 */
public class StreamUtilsTest {

	@Test
	public void testReadFully() throws Exception {
		byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		byte[] readed = new byte[8];
		StreamUtils.readFully(byteArrayInputStream, readed, 0, 8);
		Assert.assertArrayEquals(data, readed);
	}

	@Test
	public void testReadFully2() throws Exception {
		byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		byte[] readed = new byte[7];
		StreamUtils.readFully(byteArrayInputStream, readed, 0, 7);
		Assert.assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7 }, readed);
	}

	@Test
	public void testReadFully3() throws Exception {
		byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		byte[] readed = new byte[9];
		try {
			StreamUtils.readFully(byteArrayInputStream, readed, 0, 9);
			Assert.fail();
		} catch (EOFException e) {
			return;
		}

		Assert.fail();
	}
}
