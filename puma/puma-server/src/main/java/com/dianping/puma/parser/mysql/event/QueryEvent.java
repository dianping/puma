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
package com.dianping.puma.parser.mysql.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.StatusVariable;
import com.dianping.puma.parser.mysql.variable.status.QAutoIncrement;
import com.dianping.puma.parser.mysql.variable.status.QCatalogCode;
import com.dianping.puma.parser.mysql.variable.status.QCatalogNZCode;
import com.dianping.puma.parser.mysql.variable.status.QCharsetCode;
import com.dianping.puma.parser.mysql.variable.status.QCharsetDatabaseCode;
import com.dianping.puma.parser.mysql.variable.status.QFlags2Code;
import com.dianping.puma.parser.mysql.variable.status.QLCTimeNamesCode;
import com.dianping.puma.parser.mysql.variable.status.QSQLModeCode;
import com.dianping.puma.parser.mysql.variable.status.QTableMapForUpdateCode;
import com.dianping.puma.parser.mysql.variable.status.QTimeZoneCode;
import com.dianping.puma.utils.PacketUtils;

/**
 * TODO Comment of QueryEvent
 * 
 * @author Leo Liang
 * 
 */
public class QueryEvent extends AbstractBinlogEvent {

	private static final long		serialVersionUID	= 7603398043281876529L;
	private long					threadId;
	private long					execTime;
	private int						databaseNameLength;
	private int						errorCode;
	private int						statusVariablesLength;
	private List<StatusVariable>	statusVariables;
	private String					databaseName;
	private String					sql;

	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * @return the execTime
	 */
	public long getExecTime() {
		return execTime;
	}

	/**
	 * @return the databaseNameLength
	 */
	public int getDatabaseNameLength() {
		return databaseNameLength;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @return the statusVariablesLength
	 */
	public int getStatusVariablesLength() {
		return statusVariablesLength;
	}

	/**
	 * @return the statusVariables
	 */
	public List<StatusVariable> getStatusVariables() {
		return statusVariables;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		threadId = PacketUtils.readLong(buf, 4);
		execTime = PacketUtils.readLong(buf, 4);
		databaseNameLength = PacketUtils.readInt(buf, 1);
		errorCode = PacketUtils.readInt(buf, 2);
		statusVariablesLength = PacketUtils.readInt(buf, 2);
		statusVariables = parseStatusVariables(PacketUtils.readBytes(buf, statusVariablesLength));
		databaseName = PacketUtils.readNullTerminatedString(buf);
		sql = PacketUtils.readFixedLengthString(buf, buf.remaining());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueryEvent [threadId=" + threadId + ", execTime=" + execTime + ", databaseNameLength="
				+ databaseNameLength + ", errorCode=" + errorCode + ", statusVariablesLength=" + statusVariablesLength
				+ ", statusVariables=" + statusVariables + ", databaseName=" + databaseName + ", sql=" + sql
				+ ", super.toString()=" + super.toString() + "]";
	}

	protected List<StatusVariable> parseStatusVariables(byte[] data) throws IOException {
		List<StatusVariable> statusVariables = new ArrayList<StatusVariable>();
		boolean abort = false;
		ByteBuffer buf = ByteBuffer.wrap(data);
		while (!abort && buf.hasRemaining()) {
			final byte type = buf.get();
			switch (type) {
				case BinlogConstanst.Q_AUTO_INCREMENT:
					statusVariables.add(QAutoIncrement.valueOf(buf));
					break;
				case BinlogConstanst.Q_CATALOG_CODE:
					statusVariables.add(QCatalogCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_CATALOG_NZ_CODE:
					statusVariables.add(QCatalogNZCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_CHARSET_CODE:
					statusVariables.add(QCharsetCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_CHARSET_DATABASE_CODE:
					statusVariables.add(QCharsetDatabaseCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_FLAGS2_CODE:
					statusVariables.add(QFlags2Code.valueOf(buf));
					break;
				case BinlogConstanst.Q_LC_TIME_NAMES_CODE:
					statusVariables.add(QLCTimeNamesCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_SQL_MODE_CODE:
					statusVariables.add(QSQLModeCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_TABLE_MAP_FOR_UPDATE_CODE:
					statusVariables.add(QTableMapForUpdateCode.valueOf(buf));
					break;
				case BinlogConstanst.Q_TIME_ZONE_CODE:
					statusVariables.add(QTimeZoneCode.valueOf(buf));
					break;
				default:
					abort = true;
					break;
			}
		}
		return statusVariables;
	}
}
