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
 * TODO Comment of QSQLModeCode
 * 
 * @see http://code.google.com/p/open-replicator/
 * @author Leo Liang
 * 
 */
public class QSQLModeCode implements StatusVariable {

	private long	sqlMode;

	public QSQLModeCode(long sqlMode) {
		this.sqlMode = sqlMode;
	}

	public long getSqlMode() {
		return sqlMode;
	}

	public static QSQLModeCode valueOf(ByteBuffer buf) throws IOException {
		return new QSQLModeCode(PacketUtils.readLong(buf, 8));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QSQLModeCode [sqlMode=" + sqlMode + "]";
	}

}
