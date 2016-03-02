/**
 * Project: ${puma-parser.aid}
 * 
 * File Created at 2012-6-24
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
package com.dianping.puma.parser.mysql.variable.status;

import com.dianping.puma.parser.mysql.StatusVariable;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 
 * TODO Comment of QAutoIncrement
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public class QAutoIncrement implements StatusVariable {

	private int	autoIncrementIncrement;
	private int	autoIncrementOffset;

	public QAutoIncrement(int autoIncrementIncrement, int autoIncrementOffset) {
		this.autoIncrementIncrement = autoIncrementIncrement;
		this.autoIncrementOffset = autoIncrementOffset;
	}

	public int getAutoIncrementIncrement() {
		return autoIncrementIncrement;
	}

	public int getAutoIncrementOffset() {
		return autoIncrementOffset;
	}

	public static QAutoIncrement valueOf(ByteBuffer buf) throws IOException {
		return new QAutoIncrement(PacketUtils.readInt(buf, 2), PacketUtils.readInt(buf, 2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QAutoIncrement [autoIncrementIncrement=" + autoIncrementIncrement + ", autoIncrementOffset="
				+ autoIncrementOffset + "]";
	}

}
