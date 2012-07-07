/**
 * Project: puma-server
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.storage;

import junit.framework.Assert;

import org.junit.Test;

/**
 * TODO Comment of SequenceTest
 * 
 * @author Leo Liang
 * 
 */
public class SequenceTest {

	@Test
	public void testParse() throws Exception {

		Sequence seq = new Sequence(120706, 199);
		long longSeq = seq.longValue();

		Sequence parsedSeq = new Sequence(longSeq);
		Assert.assertEquals(seq.getNumber(), parsedSeq.getNumber());
		Assert.assertEquals(seq.getOffset(), parsedSeq.getOffset());
		Assert.assertEquals(seq.getCreationDate(), parsedSeq.getCreationDate());
	}

	@Test
	public void testNextSameDay() throws Exception {

		Sequence seq = new Sequence(120706, 199, 999);
		Sequence nextSeq = seq.getNext(true);
		Assert.assertEquals(200, nextSeq.getNumber());
		Assert.assertEquals(0, nextSeq.getOffset());
		Assert.assertEquals(120706, nextSeq.getCreationDate());

	}

	@Test
	public void testNextNotSameDay() throws Exception {

		Sequence seq = new Sequence(120731, 199, 999);
		Sequence nextSeq = seq.getNext(false);
		Assert.assertEquals(0, nextSeq.getNumber());
		Assert.assertEquals(0, nextSeq.getOffset());
		Assert.assertEquals(120801, nextSeq.getCreationDate());

	}
}
