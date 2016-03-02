/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-6-6 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Leo Liang
 * 
 */
public class PacketUtilTest {
    @Test
    public void testReadInt1Byte() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64 });
        Assert.assertEquals(64, PacketUtils.readInt(buf, 1));
    }

    @Test
    public void testReadInt2Byte() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64, 1 });
        Assert.assertEquals(320, PacketUtils.readInt(buf, 2));
    }

    @Test
    public void testReadInt3Byte() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64, 1, 2 });
        Assert.assertEquals(131392, PacketUtils.readInt(buf, 3));
    }

    @Test
    public void testReadInt4Byte() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64, 1, 2, 3 });
        Assert.assertEquals(50463040, PacketUtils.readInt(buf, 4));
    }

    @Test
    public void testReadIntOutOfLimit() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64, 1 });
        Assert.assertEquals(0, PacketUtils.readInt(buf, 4));
    }

    @Test
    public void testReadIntOutOfMaxInt() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 64, 1, 2, 3, 5 });
        Assert.assertEquals(0, PacketUtils.readInt(buf, 5));
    }
    
    @Test
    public void testReadNullTerminatedString(){
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{'t','e','s','t','\0'});
        Assert.assertEquals("test", PacketUtils.readNullTerminatedString(buf));
    }

}
