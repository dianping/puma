/**
 * Project: ${puma-datahandler.aid}
 * <p/>
 * File Created at 2012-6-25
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.datahandler;

import com.dianping.puma.annotation.ThreadUnSafe;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.SimpleDdlParser;
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.parser.meta.TableMetaInfo;
import com.dianping.puma.parser.meta.TableMetaInfoFetcher;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.PumaIgnoreEvent;
import com.dianping.puma.parser.mysql.event.QueryEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigInteger;

/**
 * @author Leo Liang
 */
@ThreadUnSafe
public abstract class AbstractDataHandler implements DataHandler {
    private static final Logger log = Logger.getLogger(AbstractDataHandler.class);

    private TableMetaInfoFetcher tableMetasInfoFetcher;

    /**
     * @return the tableMetasInfoFetcher
     */
    public TableMetaInfoFetcher getTableMetasInfoFetcher() {
        return tableMetasInfoFetcher;
    }

    /**
     * @param tableMetasInfoFetcher the tableMetasInfoFetcher to set
     */
    public void setTableMetasInfoFetcher(TableMetaInfoFetcher tableMetasInfoFetcher) {
        this.tableMetasInfoFetcher = tableMetasInfoFetcher;
    }

    @Override
    public void start() {
    }


    @Override
    public void stop() {

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
            BinlogInfo binlogInfo = new BinlogInfo(context.getDBServerId(), context.getBinlogFileName(),
                    context.getBinlogStartPos(), context.getEventIndex(), binlogEvent.getHeader().getTimestamp());
            result.getData().setBinlogInfo(binlogInfo);
            result.getData().setServerId(binlogEvent.getHeader().getServerId());
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

        } else if (StringUtils.equalsIgnoreCase(sql, "BEGIN")) {

            handleTransactionBeginEvent(binlogEvent, result, queryEvent);
        } else {
            result.setEmpty(true);
            result.setFinished(true);
            // log.info("QueryEvent  sql=" + queryEvent.getSql());
        }
    }

    protected void handleTransactionBeginEvent(BinlogEvent binlogEvent, DataHandlerResult result,
                                               QueryEvent queryEvent) {
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
        ddlEvent.setDdlEventType(SimpleDdlParser.getEventType(sql));

        ddlEvent.setDdlEventSubType(SimpleDdlParser.getEventSubType(ddlEvent.getDdlEventType(), sql));
        if (ddlEvent.getDdlEventType() == DdlEventType.DDL_DEFAULT
                || ddlEvent.getDdlEventSubType() == DdlEventSubType.DDL_SUB_DEFAULT) {
            log.info("DdlEvent Type do not found. ddl sql=" + sql);
        }
        SimpleDdlParser.DdlResult ddlResult = SimpleDdlParser
                .getDdlResult(ddlEvent.getDdlEventType(), ddlEvent.getDdlEventSubType(), sql);
        if (ddlResult != null) {
            ddlEvent.setDatabase(StringUtils.isNotBlank(ddlResult.getDatabase()) ? ddlResult.getDatabase()
                    : StringUtils.EMPTY);
            ddlEvent.setTable(StringUtils.isNotBlank(ddlResult.getTable()) ? ddlResult.getTable() : StringUtils.EMPTY);
            if (ddlEvent.getDdlEventType() != DdlEventType.DDL_CREATE) {
                log.info("DDL event, sql=" + sql + "  ,database =" + ddlResult.getDatabase() + " table ="
                        + ddlResult.getTable() + " queryEvent.getDatabaseName()" + queryEvent.getDatabaseName());
            }
        }
        if (StringUtils.isBlank(ddlEvent.getDatabase())) {
            ddlEvent.setDatabase(queryEvent.getDatabaseName());
        }

        if (ddlEvent.getDdlEventType() == DdlEventType.DDL_ALTER
                && ddlEvent.getDdlEventSubType() == DdlEventSubType.DDL_ALTER_TABLE) {
            ddlEvent.setDDLType(DDLType.ALTER_TABLE);
        }

        tableMetasInfoFetcher.refreshTableMeta(ddlEvent.getDatabase(), ddlEvent.getTable());

        ddlEvent.setExecuteTime(queryEvent.getHeader().getTimestamp());
        result.setData(dataChangedEvent);
        result.setEmpty(false);
        result.setFinished(true);
    }

    protected abstract void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context,
                                      byte eventType);
}
