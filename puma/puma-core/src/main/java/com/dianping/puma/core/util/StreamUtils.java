/**
 * Project: puma-core
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
package com.dianping.puma.core.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Leo Liang
 * 
 */
public class StreamUtils {
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
