/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-7 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.server.mysql.packet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.dianping.puma.common.util.PacketUtil;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractResponsePacket extends AbstractPacket implements ResponsePacket {
    private static final long serialVersionUID = 3648947016393523542L;

    protected void readHeader(InputStream is) throws IOException {
        byte[] buf = new byte[4];
        int lenRead = PacketUtil.readFully(is, buf, 0, 4);

        if (lenRead < 4) {
            // TODO close
            throw new IOException("Unexpected end of input stream");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        length = PacketUtil.readInt(byteBuffer, 3);
        seq = PacketUtil.readInt(byteBuffer, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.puma.server.mysql.packet.Packet#readPacket(java.io.InputStream
     * )
     */
    @Override
    public void readPacket(InputStream is) throws IOException {
        readHeader(is);
        byte[] buf = new byte[length + 1];
        int lenRead = PacketUtil.readFully(is, buf, 0, length);
        
        if(lenRead != length){
            throw new IOException("Short read, expected " + length + " bytes, only read " + lenRead);
        }
        
        buf[length] = 0;
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        doReadPacket(byteBuffer);

    }

    protected abstract void doReadPacket(ByteBuffer buf);
}
