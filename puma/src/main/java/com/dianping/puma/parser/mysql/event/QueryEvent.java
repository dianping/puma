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

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.StatusVariable;
import com.dianping.puma.parser.mysql.variable.status.*;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Comment of QueryEvent
 * 
 * @author Leo Liang
 * 
 */
public class QueryEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = 7603398043281876529L;
	private long threadId;
	private long execTime;
	private int databaseNameLength;
	private int errorCode;
	private int statusVariablesLength;
	private List<StatusVariable> statusVariables;
	private String databaseName;
	private String sql;

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
		int lenRemaining = lenRemaining(buf, context);
		sql = PacketUtils.readFixedLengthString(buf, lenRemaining);

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
		List<StatusVariable> parsedStatusVariables = new ArrayList<StatusVariable>();
		boolean abort = false;
		ByteBuffer buf = ByteBuffer.wrap(data);
		while (!abort && buf.hasRemaining()) {
			final byte type = buf.get();
			switch (type) {
			case BinlogConstants.Q_AUTO_INCREMENT:
				parsedStatusVariables.add(QAutoIncrement.valueOf(buf));
				break;
			case BinlogConstants.Q_CATALOG_CODE:
				parsedStatusVariables.add(QCatalogCode.valueOf(buf));
				break;
			case BinlogConstants.Q_CATALOG_NZ_CODE:
				parsedStatusVariables.add(QCatalogNZCode.valueOf(buf));
				break;
			case BinlogConstants.Q_CHARSET_CODE:
				parsedStatusVariables.add(QCharsetCode.valueOf(buf));
				break;
			case BinlogConstants.Q_CHARSET_DATABASE_CODE:
				parsedStatusVariables.add(QCharsetDatabaseCode.valueOf(buf));
				break;
			case BinlogConstants.Q_FLAGS2_CODE:
				parsedStatusVariables.add(QFlags2Code.valueOf(buf));
				break;
			case BinlogConstants.Q_LC_TIME_NAMES_CODE:
				parsedStatusVariables.add(QLCTimeNamesCode.valueOf(buf));
				break;
			case BinlogConstants.Q_SQL_MODE_CODE:
				parsedStatusVariables.add(QSQLModeCode.valueOf(buf));
				break;
			case BinlogConstants.Q_TABLE_MAP_FOR_UPDATE_CODE:
				parsedStatusVariables.add(QTableMapForUpdateCode.valueOf(buf));
				break;
			case BinlogConstants.Q_TIME_ZONE_CODE:
				parsedStatusVariables.add(QTimeZoneCode.valueOf(buf));
				break;
			default:
				abort = true;
				break;
			}
		}
		return parsedStatusVariables;
	}
}
