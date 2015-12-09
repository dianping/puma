/**
 * Project: puma-server
 * <p/>
 * File Created at 2012-7-6
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import junit.framework.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
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
    public void testSeqConstruct() throws Exception {
        Sequence seq = new Sequence(120731, 199, 999);
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
        Sequence seq = new Sequence(120731, 199, 888);
        Sequence newSeq = new Sequence(seq);
        Assert.assertEquals(199, newSeq.getNumber());
        Assert.assertEquals(888, newSeq.getOffset());
        Assert.assertEquals(120731, newSeq.getCreationDate());
        Assert.assertTrue(seq != newSeq);
    }

    @Test
    public void testNextSeqNoRenew() throws Exception {
        Sequence seq = new Sequence(120706, 199, 999);
        Sequence nextSeq = seq.getNext(false);
        Assert.assertEquals(200, nextSeq.getNumber());
        Assert.assertEquals(0, nextSeq.getOffset());
        Assert.assertEquals(120706, nextSeq.getCreationDate());
    }

    @Test
    public void testNextSeqRenew() throws Exception {
        Sequence seq = new Sequence(120731, 199, 999);
        Sequence nextSeq = seq.getNext(true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        Assert.assertEquals(0, nextSeq.getNumber());
        Assert.assertEquals(0, nextSeq.getOffset());
        Assert.assertEquals(Integer.parseInt(sdf.format(new Date())), nextSeq.getCreationDate());
    }
}
