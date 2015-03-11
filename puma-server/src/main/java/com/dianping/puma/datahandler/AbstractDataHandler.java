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
package com.dianping.puma.datahandler;

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.monitor.Notifiable;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.parser.mysql.event.QueryEvent;

/**
 * TODO Comment of AbstractDataHandler
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public abstract class AbstractDataHandler implements DataHandler, Notifiable {
	private static final Logger log = Logger.getLogger(AbstractDataHandler.class);
	private TableMetasInfoFetcher tableMetasInfoFetcher;
	private NotifyService notifyService;

	/**
	 * @return the notifyService
	 */
	public NotifyService getNotifyService() {
		return notifyService;
	}

	/**
	 * @param notifyService
	 *            the notifyService to set
	 */
	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

	/**
	 * @return the tableMetasInfoFetcher
	 */
	public TableMetasInfoFetcher getTableMetasInfoFetcher() {
		return tableMetasInfoFetcher;
	}

	/**
	 * @param tableMetasInfoFetcher
	 *            the tableMetasInfoFetcher to set
	 */
	public void setTableMetasInfoFetcher(TableMetasInfoFetcher tableMetasInfoFetcher) {
		this.tableMetasInfoFetcher = tableMetasInfoFetcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#start()
	 */
	@Override
	public void start() throws Exception {
		tableMetasInfoFetcher.refreshTableMeta();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws Exception {

	}

	protected Object convertUnsignedValueIfNeeded(int pos, Object value, TableMetaInfo tableMeta) {
		Object newValue = value;
		if (value != null) {
			switch (tableMeta.getRawTypeCodes().get(pos)) {
			case BinlogConstanst.MYSQL_TYPE_TINY:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 8));
				}
				break;
			case BinlogConstanst.MYSQL_TYPE_INT24:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 24));
				}
				break;
			case BinlogConstanst.MYSQL_TYPE_SHORT:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 16));
				}
				break;
			case BinlogConstanst.MYSQL_TYPE_INT:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Long.valueOf((Integer) value) + (1L << 32);
				} else {
					if (value instanceof Integer) {
						newValue = Long.valueOf((Integer) value);
					}
				}
				break;
			case BinlogConstanst.MYSQL_TYPE_LONGLONG:
				if ((value instanceof Long) && (Long) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = BigInteger.valueOf((Long) value).add(BigInteger.ONE.shiftLeft(64));
				} else {
					if (value instanceof Long) {
						newValue = BigInteger.valueOf((Long) value);
					}
				}
				break;
			default:
				break;
			}

		}
		return newValue;
	}

	@Override
	public DataHandlerResult process(BinlogEvent binlogEvent, PumaContext context) {
		DataHandlerResult result = new DataHandlerResult();
		if (binlogEvent instanceof PumaIgnoreEvent) {
			log.info("Ingore one unknown event. eventType: " + binlogEvent.getHeader().getEventType());
			result.setEmpty(true);
			result.setFinished(true);
			return result;
		}

		byte eventType = binlogEvent.getHeader().getEventType();
		if (log.isDebugEnabled()) {
			log.debug("event#" + eventType);
		}
		if (eventType == BinlogConstanst.STOP_EVENT || eventType == BinlogConstanst.ROTATE_EVENT) {
			result.setEmpty(true);
			result.setFinished(true);
		} else if (eventType == BinlogConstanst.FORMAT_DESCRIPTION_EVENT) {
			result.setEmpty(true);
			result.setFinished(true);
		} else if (eventType == BinlogConstanst.QUERY_EVENT) {
			handleQueryEvent(binlogEvent, result);
		} else {
			doProcess(result, binlogEvent, context, eventType);
		}

		if (result != null && !result.isEmpty() && result.getData() != null) {
			result.getData().setBinlog(context.getBinlogFileName());
			result.getData().setBinlogPos(context.getBinlogStartPos());
			result.getData().setServerId(binlogEvent.getHeader().getServerId());
			result.getData().setBinlogServerId(context.getDBServerId());
		}

		return result;
	}

	protected void handleQueryEvent(BinlogEvent binlogEvent, DataHandlerResult result) {
		QueryEvent queryEvent = (QueryEvent) binlogEvent;
		String sql = StringUtils.trim(queryEvent.getSql());
		if (StringUtils.startsWithIgnoreCase(sql, "ALTER ") || StringUtils.startsWithIgnoreCase(sql, "CREATE ")
				|| StringUtils.startsWithIgnoreCase(sql, "DROP ") || StringUtils.startsWithIgnoreCase(sql, "RENAME ")
				|| StringUtils.startsWithIgnoreCase(sql, "TRUNCATE ")) {

			handleDDlEvent(result, queryEvent, sql);

		} else if (StringUtils.equalsIgnoreCase(sql, "BEGIN")) {

			handleTransactionBeginEvent(binlogEvent, result, queryEvent);
		} else {
			result.setEmpty(true);
			result.setFinished(true);
		}
	}

	protected void handleTransactionBeginEvent(BinlogEvent binlogEvent, DataHandlerResult result, QueryEvent queryEvent) {
		// BEGIN事件，发送一个begin transaction的事件
		ChangedEvent dataChangedEvent = new RowChangedEvent();
		((RowChangedEvent) dataChangedEvent).setTransactionBegin(true);
		dataChangedEvent.setExecuteTime(binlogEvent.getHeader().getTimestamp());
		dataChangedEvent.setDatabase(queryEvent.getDatabaseName());

		result.setData(dataChangedEvent);
		result.setEmpty(false);
		result.setFinished(true);
	}

	/**
	 * @param result
	 * @param queryEvent
	 * @param sql
	 */
	protected void handleDDlEvent(DataHandlerResult result, QueryEvent queryEvent, String sql) {
		log.info("ddl:" + sql + ", from " + queryEvent.getDatabaseName());
		String db = "";
		try {
			db = sql.substring(0, sql.indexOf("."));
			db = db.substring(db.indexOf("`") + 1, db.indexOf("`", db.indexOf("`") + 1));
		} catch (RuntimeException e) {
			log.warn("", e);
		}
		if (db != null && db.length() > 0 && !db.equalsIgnoreCase(queryEvent.getDatabaseName())) {
			log.info("ddl ignored:" + sql + ", from " + queryEvent.getDatabaseName());
		} else {
			// Refresh table meta
			tableMetasInfoFetcher.refreshTableMeta();
			log.info("ddl refreshed meta");
		}
		ChangedEvent dataChangedEvent = new DdlEvent();
		DdlEvent ddlEvent = (DdlEvent) dataChangedEvent;
		ddlEvent.setSql(sql);
		ddlEvent.setDatabase(queryEvent.getDatabaseName());
		ddlEvent.setExecuteTime(queryEvent.getHeader().getTimestamp());

		result.setData(dataChangedEvent);
		result.setEmpty(false);
		result.setFinished(true);
	}

	protected abstract void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context,
			byte eventType);

}
