/**
 * Project: ${puma-server.aid}
 * <p/>
 * File Created at 2012-6-7 $Id$
 * <p/>
 * Copyright 2010 dianping.com. All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Leo Liang
 */
public abstract class AbstractResponsePacket extends AbstractPacket implements ResponsePacket {
    private static final long serialVersionUID = 3648947016393523542L;

    public static final int MAX_PACKET_LENGTH = (256 * 256 * 256 - 1);

    protected void readHeader(InputStream is) throws IOException {
        byte[] buf = new byte[4];
        int lenRead = PacketUtils.readFully(is, buf, 0, 4);

        if (lenRead < 4) {
            // TODO close
            throw new IOException("Unexpected end of input stream");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        length = PacketUtils.readInt(byteBuffer, 3);
        seq = PacketUtils.readInt(byteBuffer, 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dianping.puma.server.mysql.packet.Packet#readPacket(java.io.InputStream
     * )
     */
    @Override
    public void readPacket(InputStream is, PumaContext context) throws IOException {
        readPacket(is, context, null);
    }

    protected void readPacket(InputStream is, PumaContext context, List<byte[]> moreBuf) throws IOException {
        readHeader(is);
        byte[] buf = new byte[length];
        int lenRead = 0;
        lenRead = PacketUtils.readFully(is, buf, 0, length);

        if (lenRead != length) {
            throw new IOException("Short read, expected " + length + " bytes, only read " + lenRead);
        }

        if (MAX_PACKET_LENGTH == length) {
            moreBuf = moreBuf == null ? new LinkedList<byte[]>() : moreBuf;
            moreBuf.add(buf);
            readPacket(is, context, moreBuf);
        } else {
            ByteBuffer byteBuffer;
            if (moreBuf == null) {
                byteBuffer = ByteBuffer.wrap(buf);
            } else {
                moreBuf.add(buf);
                byteBuffer = ByteBuffer.wrap(combinePacket(moreBuf));
            }

            doReadPacket(byteBuffer, context);
        }
    }

    private byte[] combinePacket(List<byte[]> moreBuf) {
        int size = 0;
        for (byte[] data : moreBuf) {
            size += data.length;
        }

        byte[] finalBuf = new byte[size];

        int destPos = 0;
        for (byte[] data : moreBuf) {
            System.arraycopy(data, 0, finalBuf, destPos, data.length);
            destPos += data.length;
        }
        return finalBuf;
    }

    protected abstract void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException;
}
