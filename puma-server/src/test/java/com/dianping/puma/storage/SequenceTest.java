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

import java.text.SimpleDateFormat;
import java.util.Date;

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

		Sequence parsedSeq = new Sequence(longSeq, 0);
		Assert.assertEquals(seq.getNumber(), parsedSeq.getNumber());
		Assert.assertEquals(seq.getOffset(), parsedSeq.getOffset());
		Assert.assertEquals(seq.getCreationDate(), parsedSeq.getCreationDate());
	}

	@Test
	public void testSeqConstruct() throws Exception {
		Sequence seq = new Sequence(120731, 199, 999, 0);
		Assert.assertEquals(199, seq.getNumber());
		Assert.assertEquals(999, seq.getOffset());
		Assert.assertEquals(120731, seq.getCreationDate());
	}

	@Test
	public void testSeqConstructWithoutOffset() throws Exception {
		Sequence seq = new Sequence(120731, 199);
		Assert.assertEquals(199, seq.getNumber());
		Assert.assertEquals(0, seq.getOffset());
		Assert.assertEquals(120731, seq.getCreationDate());
	}

	@Test
	public void testSeqCopyConstruct() throws Exception {
		Sequence seq = new Sequence(120731, 199, 888, 0);
		Sequence newSeq = new Sequence(seq);
		Assert.assertEquals(199, newSeq.getNumber());
		Assert.assertEquals(888, newSeq.getOffset());
		Assert.assertEquals(120731, newSeq.getCreationDate());
		Assert.assertTrue(seq != newSeq);
	}

	@Test
	public void testNextSeqNoRenew() throws Exception {
		Sequence seq = new Sequence(120706, 199, 999, 0);
		Sequence nextSeq = seq.getNext(false);
		Assert.assertEquals(200, nextSeq.getNumber());
		Assert.assertEquals(0, nextSeq.getOffset());
		Assert.assertEquals(120706, nextSeq.getCreationDate());
	}

	@Test
	public void testNextSeqRenew() throws Exception {
		Sequence seq = new Sequence(120731, 199, 999, 0);
		Sequence nextSeq = seq.getNext(true);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Assert.assertEquals(0, nextSeq.getNumber());
		Assert.assertEquals(0, nextSeq.getOffset());
		Assert.assertEquals(Integer.parseInt(sdf.format(new Date())), nextSeq.getCreationDate());
	}

	@Test
	public void testAddOffset() throws Exception {
		Sequence seq = new Sequence(120731, 199, 999, 0);
		Sequence newSeq = seq.addOffset(1000);
		Assert.assertEquals(199, newSeq.getNumber());
		Assert.assertEquals(1999, newSeq.getOffset());
		Assert.assertEquals(120731, newSeq.getCreationDate());
		Assert.assertTrue(seq != newSeq);
	}

	@Test
	public void testClearOffset() throws Exception {
		Sequence seq = new Sequence(120731, 199, 999, 0);
		Sequence newSeq = seq.clearOffset();
		Assert.assertEquals(199, newSeq.getNumber());
		Assert.assertEquals(0, newSeq.getOffset());
		Assert.assertEquals(120731, newSeq.getCreationDate());
		Assert.assertTrue(seq != newSeq);
	}
}
