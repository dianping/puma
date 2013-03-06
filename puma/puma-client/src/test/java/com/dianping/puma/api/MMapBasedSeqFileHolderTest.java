/**
 * Project: puma-client
 * 
 * File Created at 2012-7-9
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
package com.dianping.puma.api;

import junit.framework.Assert;

import org.junit.Test;

/**
 * TODO Comment of MMapBasedSeqFileHolderTest
 * 
 * @author Leo Liang
 * 
 */
public class MMapBasedSeqFileHolderTest {
	@Test
	public void test() {
		int times = 100000;
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.host("192.168.1.23");
		builder.port(23);
		builder.seqFileBase(System.getProperty("java.io.tmpdir", ".") + "test");
		MMapBasedSeqFileHolder holder = new MMapBasedSeqFileHolder(builder.build());
		long start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			holder.saveSeq((long) i);
		}
		System.out.println("MMapBaseSeqFileHolder save " + times + " times spend "
				+ (System.currentTimeMillis() - start) + "ms");
		Assert.assertEquals(times - 1, holder.getSeq());
	}
}
