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
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.host("1111");
		builder.port(23);
		builder.seqFileBase("/Users/leoleung/1/");
		MMapBasedSeqFileHolder holder = new MMapBasedSeqFileHolder(builder.build());
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000007; i++) {
			holder.saveSeq((long) i);
		}
		System.out.println((System.currentTimeMillis() - start));
		Assert.assertEquals(1000006L, holder.getSeq());
	}
}
