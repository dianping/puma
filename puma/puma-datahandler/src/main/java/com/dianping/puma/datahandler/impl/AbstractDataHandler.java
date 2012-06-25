/**
 * Project: ${puma-datahandler.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.datahandler.impl;

import java.sql.Blob;

import org.apache.log4j.Logger;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.client.RowChangedData.ColumnType;
import com.dianping.puma.client.TableMetaInfo;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.column.BitColumn;
import com.dianping.puma.common.mysql.column.Column;
import com.dianping.puma.common.mysql.column.DateColumn;
import com.dianping.puma.common.mysql.column.DatetimeColumn;
import com.dianping.puma.common.mysql.column.DecimalColumn;
import com.dianping.puma.common.mysql.column.DoubleColumn;
import com.dianping.puma.common.mysql.column.EnumColumn;
import com.dianping.puma.common.mysql.column.FloatColumn;
import com.dianping.puma.common.mysql.column.Int24Column;
import com.dianping.puma.common.mysql.column.IntColumn;
import com.dianping.puma.common.mysql.column.LongLongColumn;
import com.dianping.puma.common.mysql.column.NullColumn;
import com.dianping.puma.common.mysql.column.SetColumn;
import com.dianping.puma.common.mysql.column.ShortColumn;
import com.dianping.puma.common.mysql.column.StringColumn;
import com.dianping.puma.common.mysql.column.TimeColumn;
import com.dianping.puma.common.mysql.column.TimestampColumn;
import com.dianping.puma.common.mysql.column.TinyColumn;
import com.dianping.puma.common.mysql.column.YearColumn;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.datahandler.DataHandler;

/**
 * TODO Comment of AbstractDataHandler
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDataHandler implements DataHandler {
	private static final Logger	log	= Logger.getLogger(AbstractDataHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {

	}

	@Override
	public DataChangedEvent process(BinlogEvent binlogEvent, PumaContext context) {
		if (binlogEvent instanceof PumaIgnoreEvent) {
			log.info("Ingore one unknown event. eventType: " + binlogEvent.getHeader().getEventType());
		}

		byte eventType = binlogEvent.getHeader().getEventType();

		if (eventType == BinlogConstanst.STOP_EVENT || eventType == BinlogConstanst.ROTATE_EVENT) {
			return null;
		} else {
			return doProcess(binlogEvent, context, eventType);
		}
	}

	protected abstract DataChangedEvent doProcess(BinlogEvent binlogEvent, PumaContext context, byte eventType);

	protected TableMetaInfo getTableMetaInfo(String database, String table) {
		// TODO
		return null;
	}

	protected ColumnType getColumnType(Column column) {
		if (column instanceof BitColumn) {
			return ColumnType.BYTEARRAY;
		} else if (column instanceof Blob) {
			return ColumnType.BYTEARRAY;
		} else if (column instanceof DateColumn) {
			return ColumnType.SQLDATE;
		} else if (column instanceof DatetimeColumn) {
			return ColumnType.JAVADATE;
		} else if (column instanceof DecimalColumn) {
			return ColumnType.BIGDECIMAL;
		} else if (column instanceof DoubleColumn) {
			return ColumnType.DOUBLE;
		} else if (column instanceof EnumColumn) {
			return ColumnType.INTEGER;
		} else if (column instanceof FloatColumn) {
			return ColumnType.FLOAT;
		} else if (column instanceof Int24Column) {
			return ColumnType.INTEGER;
		} else if (column instanceof IntColumn) {
			return ColumnType.INTEGER;
		} else if (column instanceof LongLongColumn) {
			return ColumnType.LONG;
		} else if (column instanceof NullColumn) {
			return ColumnType.NULL;
		} else if (column instanceof SetColumn) {
			return ColumnType.LONG;
		} else if (column instanceof ShortColumn) {
			return ColumnType.INTEGER;
		} else if (column instanceof StringColumn) {
			return ColumnType.STRING;
		} else if (column instanceof TimeColumn) {
			return ColumnType.SQLTIME;
		} else if (column instanceof TimestampColumn) {
			return ColumnType.SQLTIMESTAMP;
		} else if (column instanceof TinyColumn) {
			return ColumnType.INTEGER;
		} else if (column instanceof YearColumn) {
			return ColumnType.INTEGER;
		} else {
			return null;
		}
	}

	protected String getColumnName(long columnId) {
		// TODO
		return String.valueOf(columnId);
	}
}
