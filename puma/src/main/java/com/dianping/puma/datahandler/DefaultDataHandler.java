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

import com.dianping.cat.Cat;
import com.dianping.puma.annotation.ThreadUnSafe;
import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.parser.meta.TableMetaInfo;
import com.dianping.puma.parser.mysql.BinlogConstants;
import com.dianping.puma.parser.mysql.column.Column;
import com.dianping.puma.parser.mysql.event.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leo Liang
 */
@ThreadUnSafe
public class DefaultDataHandler extends AbstractDataHandler {
    private Logger log = Logger.getLogger(DefaultDataHandler.class);

    private Map<Long, TableMetaInfo> tableMetaInfos;

    private int rowPos = 0;

    @Override
    protected void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context, byte eventType) {
        if (log.isDebugEnabled()) {
            log.debug("event:" + eventType);
        }
        switch (eventType) {
            case BinlogConstants.TABLE_MAP_EVENT:
                TableMapEvent tableMapEvent = (TableMapEvent) binlogEvent;

                if (tableMetaInfos == null) {
                    tableMetaInfos = new HashMap<Long, TableMetaInfo>();
                }

                TableMetaInfo tableMetaInfo = getTableMetasInfoFetcher().getTableMetaInfo(tableMapEvent.getDatabaseName(),
                        tableMapEvent.getTableName());

                if (tableMetaInfo != null) {
                    tableMetaInfos.put(tableMapEvent.getTableId(), tableMetaInfo);
                    if (log.isDebugEnabled()) {
                        log.debug("put meta info for table id:" + tableMapEvent.getTableId());
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("meta info not found for:" + tableMapEvent.getDatabaseName() + "-"
                                + tableMapEvent.getTableName());
                    }
                    skipEvent(BinlogConstants.TABLE_MAP_EVENT, result, context);
                    return;
                }

                fillRawTypeCodes(tableMapEvent, tableMetaInfo);
                fillRawNullAbilities(tableMapEvent, tableMetaInfo);
                rowPos = 0;
                result.setEmpty(true);
                result.setFinished(true);
                break;
            case BinlogConstants.WRITE_ROWS_EVENT_V1:
            case BinlogConstants.WRITE_ROWS_EVENT:
                if (tableMetaInfos == null || tableMetaInfos.isEmpty()) {
                    skipEvent(BinlogConstants.WRITE_ROWS_EVENT, result, context);
                    return;
                }

                processWriteRowEvent(result, binlogEvent, context);
                break;
            case BinlogConstants.UPDATE_ROWS_EVENT_V1:
            case BinlogConstants.UPDATE_ROWS_EVENT:
                if (tableMetaInfos == null || tableMetaInfos.isEmpty()) {
                    skipEvent(BinlogConstants.UPDATE_ROWS_EVENT, result, context);
                    return;
                }
                processUpdateRowEvent(result, binlogEvent, context);
                break;
            case BinlogConstants.DELETE_ROWS_EVENT_V1:
            case BinlogConstants.DELETE_ROWS_EVENT:
                if (tableMetaInfos == null || tableMetaInfos.isEmpty()) {
                    skipEvent(BinlogConstants.DELETE_ROWS_EVENT, result, context);
                    return;
                }
                processDeleteRowEvent(result, binlogEvent, context);
                break;
            case BinlogConstants.XID_EVENT:
                if (tableMetaInfos == null || tableMetaInfos.isEmpty()) {
                    skipEvent(BinlogConstants.XID_EVENT, result, context);
                    return;
                }
                processTransactionCommitEvent(binlogEvent, result);
                break;
            default:
                result.setEmpty(true);
                result.setFinished(true);
                break;
        }

    }

    protected void processTransactionCommitEvent(BinlogEvent binlogEvent, DataHandlerResult result) {
        // commit事件，发送一个commit transaction的事件
        ChangedEvent dataChangedEvent = new RowChangedEvent();
        ((RowChangedEvent) dataChangedEvent).setTransactionCommit(true);
        dataChangedEvent.setExecuteTime(binlogEvent.getHeader().getTimestamp());
        dataChangedEvent.setDatabase(tableMetaInfos.values().iterator().next().getDatabase());

        result.setData(dataChangedEvent);
        result.setEmpty(false);
        result.setFinished(true);
        tableMetaInfos.clear();
        tableMetaInfos = null;
    }

    /**
     * @param result
     * @param binlogEvent
     * @param context
     */
    protected void processDeleteRowEvent(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context) {
        DeleteRowsEvent deleteRowsEvent = (DeleteRowsEvent) binlogEvent;

        if (rowPos >= deleteRowsEvent.getRows().size()) {
            rowPos = 0;
            result.setEmpty(true);
            result.setFinished(true);
        } else {
            TableMetaInfo tableMetaInfo = tableMetaInfos.get(deleteRowsEvent.getTableId());

            if (tableMetaInfo == null) {
                skipEvent(BinlogConstants.DELETE_ROWS_EVENT, result, context);
                return;
            }

            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(deleteRowsEvent, rowChangedEvent, DMLType.DELETE,
                    tableMetaInfo);

            for (int columnPos = 0, columnIndex = 0; columnPos < deleteRowsEvent.getColumnCount().intValue(); columnPos++) {
                if (deleteRowsEvent.getUsedColumns().get(columnPos)) {
                    Column binlogColumn = deleteRowsEvent.getRows().get(rowPos).getColumns().get(columnIndex);
                    String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                    if (!checkUnknownColumnName(result, context, columnName, columnPos + 1)) {
                        return;
                    }
                    ColumnInfo columnInfo = new ColumnInfo(tableMetaInfo.getKeys().contains(columnName),
                            convertUnsignedValueIfNeeded(columnPos + 1, binlogColumn.getValue(), tableMetaInfo), null);
                    columns.put(columnName, columnInfo);
                    columnIndex++;
                }
            }

            rowPos++;
            result.setData(rowChangedEvent);
            result.setEmpty(false);
            result.setFinished(false);
        }
    }

    /**
     * @param result
     * @param binlogEvent
     * @param context
     */
    protected void processUpdateRowEvent(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context) {
        UpdateRowsEvent updateRowsEvent = (UpdateRowsEvent) binlogEvent;

        if (rowPos >= updateRowsEvent.getRows().size()) {
            rowPos = 0;
            result.setEmpty(true);
            result.setFinished(true);
        } else {
            TableMetaInfo tableMetaInfo = tableMetaInfos.get(updateRowsEvent.getTableId());

            if (tableMetaInfo == null) {
                skipEvent(BinlogConstants.UPDATE_ROWS_EVENT, result, context);
                return;
            }

            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(updateRowsEvent, rowChangedEvent, DMLType.UPDATE,
                    tableMetaInfo);
            if (log.isDebugEnabled()) {
                log.debug("update from " + tableMetaInfo.getDatabase() + "." + tableMetaInfo.getTable());
            }

            for (int columnPos = 0, columnAfterIndex = 0, columnBeforeIndex = 0; columnPos < updateRowsEvent
                    .getColumnCount().intValue(); columnPos++) {
                String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                if (!checkUnknownColumnName(result, context, columnName, columnPos + 1)) {
                    return;
                }
                Column afterColumn = null;
                Column beforeColumn = null;
                if (updateRowsEvent.getUsedColumnsAfter().get(columnPos)) {
                    afterColumn = updateRowsEvent.getRows().get(rowPos).getAfter().getColumns().get(columnAfterIndex);
                    columnAfterIndex++;
                }
                if (updateRowsEvent.getUsedColumnsBefore().get(columnPos)) {
                    beforeColumn = updateRowsEvent.getRows().get(rowPos).getBefore().getColumns().get(columnBeforeIndex);
                    columnBeforeIndex++;
                }
                ColumnInfo columnInfo = new ColumnInfo(tableMetaInfo.getKeys().contains(columnName),
                        beforeColumn == null ? null : convertUnsignedValueIfNeeded(columnPos + 1, beforeColumn.getValue(),
                                tableMetaInfo), afterColumn == null ? null : convertUnsignedValueIfNeeded(columnPos + 1,
                        afterColumn.getValue(), tableMetaInfo));
                columns.put(columnName, columnInfo);
            }

            rowPos++;
            result.setData(rowChangedEvent);
            result.setEmpty(false);
            result.setFinished(false);
        }
    }

    /**
     * @param result
     * @param binlogEvent
     * @param context
     */
    protected void processWriteRowEvent(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context) {
        WriteRowsEvent writeRowsEvent = (WriteRowsEvent) binlogEvent;

        if (rowPos >= writeRowsEvent.getRows().size()) {
            rowPos = 0;
            result.setEmpty(true);
            result.setFinished(true);
        } else {
            TableMetaInfo tableMetaInfo = tableMetaInfos.get(writeRowsEvent.getTableId());

            if (tableMetaInfo == null) {
                skipEvent(BinlogConstants.WRITE_ROWS_EVENT, result, context);
                return;
            }

            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(writeRowsEvent, rowChangedEvent, DMLType.INSERT,
                    tableMetaInfo);

            for (int columnPos = 0, columnIndex = 0; columnPos < writeRowsEvent.getColumnCount().intValue(); columnPos++) {
                if (writeRowsEvent.getUsedColumns().get(columnPos)) {
                    Column binlogColumn = writeRowsEvent.getRows().get(rowPos).getColumns().get(columnIndex);
                    String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                    if (!checkUnknownColumnName(result, context, columnName, columnPos + 1)) {
                        return;
                    }
                    ColumnInfo columnInfo = new ColumnInfo(tableMetaInfo.getKeys().contains(columnName), null,
                            convertUnsignedValueIfNeeded(columnPos + 1, binlogColumn.getValue(), tableMetaInfo));
                    columns.put(columnName, columnInfo);
                    columnIndex++;
                }
            }

            rowPos++;
            result.setData(rowChangedEvent);
            result.setEmpty(false);
            result.setFinished(false);
        }
    }

    protected boolean checkUnknownColumnName(DataHandlerResult result, PumaContext context, String columnName, int pos) {
        if (columnName == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unknown column for Binlog:  ").append(context.getBinlogFileName()).append(" BinlogPos: ")
                    .append(context.getBinlogStartPos()).append(" Skip to ").append(context.getNextBinlogPos());
            msg.append(" columnPos: ").append(pos);
            log.error(msg.toString());
            Cat.logError(msg.toString(), new IllegalArgumentException(msg.toString()));
            skipEvent((byte) 0, result, context);

            return false;
        }

        return true;
    }

    protected void skipEvent(byte eventType, DataHandlerResult result, PumaContext context) {
        rowPos = 0;
        result.setEmpty(true);
        result.setFinished(true);
        StringBuilder msg = new StringBuilder();
        msg.append("Skip one event#").append(eventType).append(", since there is no table meta info. Binlog: ")
                .append(context.getBinlogFileName()).append(" Pos: ").append(context.getBinlogStartPos())
                .append(" Skip to ").append(context.getNextBinlogPos());
        if (log.isDebugEnabled()) {
            log.debug(msg.toString());
        }
    }

    /**
     * @param tableMapEvent
     * @param tableMeta
     */
    private void fillRawNullAbilities(TableMapEvent tableMapEvent, TableMetaInfo tableMeta) {
        if (tableMeta.getRawNullAbilities() == null && tableMapEvent.getColumnNullabilities() != null) {
            List<Integer> rawNullAbilities = new ArrayList<Integer>();
            for (int i = 0; i < tableMapEvent.getColumnNullabilities().size()
                    && i < tableMapEvent.getColumnCount().intValue(); i++) {
                if (tableMapEvent.getColumnNullabilities().get(i)) {
                    rawNullAbilities.add(i + 1);
                }
            }

            tableMeta.setRawNullAbilities(rawNullAbilities);
        }
    }

    /**
     * @param tableMapEvent
     * @param tableMeta
     */
    private void fillRawTypeCodes(TableMapEvent tableMapEvent, TableMetaInfo tableMeta) {
        if (tableMeta.getRawTypeCodes() == null && tableMapEvent.getColumnTypes() != null) {
            Map<Integer, Byte> rawTypes = new HashMap<Integer, Byte>();
            for (int i = 0; i < tableMapEvent.getColumnTypes().length; i++) {
                rawTypes.put(i + 1, tableMapEvent.getColumnTypes()[i]);
            }

            tableMeta.setRawTypeCodes(rawTypes);
        }
    }

    private Map<String, ColumnInfo> initColumns(AbstractRowsEvent rowsEvent, RowChangedEvent rowChangedData,
                                                DMLType dmlType, TableMetaInfo tableMetaInfo) {
        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
        rowChangedData.setDmlType(dmlType);
        rowChangedData.setExecuteTime(rowsEvent.getHeader().getTimestamp());
        rowChangedData.setColumns(columns);
        rowChangedData.setDatabase(tableMetaInfo.getDatabase());
        rowChangedData.setTable(tableMetaInfo.getTable());
        return columns;
    }
}
