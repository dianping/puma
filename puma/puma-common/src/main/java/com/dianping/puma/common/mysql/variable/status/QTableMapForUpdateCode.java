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
package com.dianping.puma.common.mysql.variable.status;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.mysql.StatusVariable;
import com.dianping.puma.common.util.PacketUtils;

/**
 * 
 * TODO Comment of QTableMapForUpdateCode
 * 
 * @author Leo Liang
 * 
 */
public class QTableMapForUpdateCode implements StatusVariable {

	private long	tableMap;

	public QTableMapForUpdateCode(long tableMap) {
		this.tableMap = tableMap;
	}

	public long getTableMap() {
		return tableMap;
	}

	public static QTableMapForUpdateCode valueOf(ByteBuffer buf) throws IOException {
		return new QTableMapForUpdateCode(PacketUtils.readLong(buf, 8));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QTableMapForUpdateCode [tableMap=" + tableMap + "]";
	}

}
