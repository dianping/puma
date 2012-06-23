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
package com.dianping.puma.common.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.datatype.UnsignedLong;

/**
 * @author Leo Liang
 * 
 */
public class PacketUtils {

    public static int readInt(ByteBuffer buf, int length) {
        if ((buf.position() + length) <= buf.limit() && length <= 4) {
            int r = 0;
            for (int i = 0; i < length; i++) {
                r |= ((buf.get() & 0xff) << (i << 3));
            }
            return r;
        }
        return 0;
    }

    public static String readNullTerminatedString(ByteBuffer buf) {
        return readNullTerminatedString(buf, "ASCII");
    }

    public static String readNullTerminatedString(ByteBuffer buf, String encoding) {
        int start = buf.position();
        int len = 0;
        int maxLen = buf.limit();

        while ((buf.position() < maxLen) && (buf.get() != 0)) {
            len++;
        }

        try {
            return new String(buf.array(), start, len, encoding);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static long readLong(ByteBuffer buf, int length) {
        if ((buf.position() + length) <= buf.limit() && length <= 8) {
            long r = 0;
            for (int i = 0; i < length; i++) {
                r |= (((long) buf.get() & 0xff) << (i << 3));
            }
            return r;
        }
        return 0;
    }

    public static byte[] readBytes(ByteBuffer buf, int length) {
        if ((buf.position() + length) <= buf.limit()) {
            byte[] r = new byte[length];
            for (int i = 0; i < length; i++) {
                r[i] = buf.get();
            }
            return r;
        }
        return null;
    }

    public static UnsignedLong readLengthCodedUnsignedLong(ByteBuffer buf) {
        byte v = buf.get();
        if (v < 251)
            return UnsignedLong.asUnsigned(v);
        else if (v == 251)
            return null;
        else if (v == 252)
            return UnsignedLong.asUnsigned(readInt(buf, 2));
        else if (v == 253)
            return UnsignedLong.asUnsigned(readInt(buf, 3));
        else if (v == 254)
            return UnsignedLong.asUnsigned(readInt(buf, 8));
        else
            return null;
    }
    
    public static String readLengthCodedString(ByteBuffer buf) throws IOException {
        final UnsignedLong length = readLengthCodedUnsignedLong(buf);
        return length == null ? null : readFixedLengthString(buf, length.intValue()); 
    }
    
    public static String readFixedLengthString(ByteBuffer buf, int length) throws IOException {
        return new String(readBytes(buf, length));
    }

    public static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException("Can not read response from server. Expected to " + len + " bytes, read " + n
                        + " bytes before connection was unexpectedly lost.");
            }

            n += count;
        }
        return n;
    }

    public static void writeLong(ByteBuffer buf, long value, int len) {
        for (int i = 0; i < len; i++) {
            buf.put((byte) ((value >>> (i << 3)) & 0xff));
        }
    }

    public static void writeInt(ByteBuffer buf, int value, int len) {
        for (int i = 0; i < len; i++) {
            buf.put((byte) ((value >>> (i << 3)) & 0xff));
        }
    }

    public static void writeByte(ByteBuffer buf, byte b) {
        buf.put(b);
    }

    public static final void writeBytesNoNull(ByteBuffer buf, byte[] bytes) {
        buf.put(bytes);
    }

    public static void writeNullTerminatedString(ByteBuffer buf, String s, String encoding) throws IOException {
        buf.put(s.getBytes(encoding));
        buf.put((byte) 0);
    }

}
