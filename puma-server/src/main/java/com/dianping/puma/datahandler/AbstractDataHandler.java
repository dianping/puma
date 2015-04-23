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
import com.dianping.puma.core.util.SimpleDdlParser;
import com.dianping.puma.core.util.SimpleDdlParser.DdlResult;
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import com.dianping.puma.parser.mysql.BinlogConstants;
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
		tableMetasInfoFetcher.refreshTableMeta(null, null, true);
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
			case BinlogConstants.MYSQL_TYPE_TINY:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 8));
				}
				break;
			case BinlogConstants.MYSQL_TYPE_INT24:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 24));
				}
				break;
			case BinlogConstants.MYSQL_TYPE_SHORT:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Integer.valueOf((Integer) value + (1 << 16));
				}
				break;
			case BinlogConstants.MYSQL_TYPE_INT:
				if ((value instanceof Integer) && (Integer) value < 0 && !tableMeta.getSignedInfos().get(pos)) {
					newValue = Long.valueOf((Integer) value) + (1L << 32);
				} else {
					if (value instanceof Integer) {
						newValue = Long.valueOf((Integer) value);
					}
				}
				break;
			case BinlogConstants.MYSQL_TYPE_LONGLONG:
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
		if (eventType == BinlogConstants.STOP_EVENT || eventType == BinlogConstants.ROTATE_EVENT) {
			result.setEmpty(true);
			result.setFinished(true);
		} else if (eventType == BinlogConstants.FORMAT_DESCRIPTION_EVENT) {
			result.setEmpty(true);
			result.setFinished(true);
		} else if (eventType == BinlogConstants.QUERY_EVENT) {
			handleQueryEvent(binlogEvent, result);
		} else {
			doProcess(result, binlogEvent, context, eventType);
		}

		if (result != null && !result.isEmpty() && result.getData() != null) {
			result.getData().setBinlog(context.getBinlogFileName());
			result.getData().setBinlogPos(context.getBinlogStartPos());
			result.getData().setBinlogNextPos(binlogEvent.getHeader().getNextPosition());
			result.getData().setServerId(binlogEvent.getHeader().getServerId());
			result.getData().setBinlogServerId(context.getDBServerId());
		}

		return result;
	}

	protected void handleQueryEvent(BinlogEvent binlogEvent, DataHandlerResult result) {
		QueryEvent queryEvent = (QueryEvent) binlogEvent;
		String sql = StringUtils.normalizeSpace(queryEvent.getSql());
		if (StringUtils.startsWithIgnoreCase(sql, "ALTER ") || StringUtils.startsWithIgnoreCase(sql, "CREATE ")
				|| StringUtils.startsWithIgnoreCase(sql, "DROP ") || StringUtils.startsWithIgnoreCase(sql, "RENAME ")
				|| StringUtils.startsWithIgnoreCase(sql, "TRUNCATE ")) {

			handleDDlEvent(result, queryEvent, sql);
			if (!StringUtils.startsWithIgnoreCase(sql, "CREATE ")){
				log.info("DdlEvent ddl sql=" + sql);
			}

		} else if (StringUtils.equalsIgnoreCase(sql, "BEGIN")) {

			handleTransactionBeginEvent(binlogEvent, result, queryEvent);
		} else {
			result.setEmpty(true);
			result.setFinished(true);
			//log.info("QueryEvent  sql=" + queryEvent.getSql());
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
		ChangedEvent dataChangedEvent = new DdlEvent();
		DdlEvent ddlEvent = (DdlEvent) dataChangedEvent;
		ddlEvent.setSql(sql);
		ddlEvent.setEventType(SimpleDdlParser.getEventType(sql));

		ddlEvent.setEventSubType(SimpleDdlParser.getEventSubType(ddlEvent.getEventType(), sql));
		if (ddlEvent.getEventType() == DdlEventType.DDL_DEFAULT
				|| ddlEvent.getEventSubType() == DdlEventSubType.DDL_SUB_DEFAULT) {
			log.info("DdlEvent Type do not found. ddl sql=" + sql);
		}
		DdlResult ddlResult = SimpleDdlParser.getDdlResult(ddlEvent.getEventType(), ddlEvent.getEventSubType(), sql);
		if (ddlResult != null) {
			ddlEvent.setDatabase(StringUtils.isNotBlank(ddlResult.getDatabase()) ? ddlResult.getDatabase()
					: StringUtils.EMPTY);
			ddlEvent.setTable(StringUtils.isNotBlank(ddlResult.getTable()) ? ddlResult.getTable() : StringUtils.EMPTY);
			if (ddlEvent.getEventType() != DdlEventType.DDL_CREATE) {
				log.info("DDL event, sql=" + sql + "  ,database =" + ddlResult.getDatabase() + " table ="
						+ ddlResult.getTable() + " queryEvent.getDatabaseName()" + queryEvent.getDatabaseName());
			}
		} else {
			if (ddlEvent.getEventType() != DdlEventType.DDL_CREATE)
				log.info("DDL event unable to get ddlResult , sql=" + sql);
		}
		if (StringUtils.isBlank(ddlEvent.getDatabase())) {
			ddlEvent.setDatabase(queryEvent.getDatabaseName());
		}
		// 过滤系统的ddl引起的refresh慢查询
		/*
		 * if (!(StringUtils.isNotBlank(ddlEvent.getDatabase()) &&
		 * TableMetaRefreshFilter
		 * .instance.getFiltedDatabases().contains(ddlEvent
		 * .getDatabase().toLowerCase()))) {
		 * tableMetasInfoFetcher.refreshTableMeta
		 * (ddlEvent.getDatabase().toLowerCase(),false);
		 * log.info("table meta refresh.    DDL event sql:"+sql+"."); }
		 */
		tableMetasInfoFetcher.refreshTableMeta(ddlEvent.getDatabase(), ddlEvent.getTable(), false);

		ddlEvent.setExecuteTime(queryEvent.getHeader().getTimestamp());
		result.setData(dataChangedEvent);
		result.setEmpty(false);
		result.setFinished(true);
	}

	protected abstract void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context,
			byte eventType);

}
