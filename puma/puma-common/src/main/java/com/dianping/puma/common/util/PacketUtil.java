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

/**
 * @author Leo Liang
 * 
 */
public class PacketUtil {

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
}
