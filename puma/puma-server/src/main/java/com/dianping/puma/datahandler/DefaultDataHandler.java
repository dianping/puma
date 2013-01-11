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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.parser.mysql.BinlogConstanst;
import com.dianping.puma.parser.mysql.column.Column;
import com.dianping.puma.parser.mysql.event.AbstractRowsEvent;
import com.dianping.puma.parser.mysql.event.BinlogEvent;
import com.dianping.puma.parser.mysql.event.DeleteRowsEvent;
import com.dianping.puma.parser.mysql.event.TableMapEvent;
import com.dianping.puma.parser.mysql.event.UpdateRowsEvent;
import com.dianping.puma.parser.mysql.event.WriteRowsEvent;

/**
 * TODO Comment of TransactionSupportDataHandler
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public class DefaultDataHandler extends AbstractDataHandler {
    private Logger        log    = Logger.getLogger(DefaultDataHandler.class);
    private TableMetaInfo tableMetaInfo;
    private int           rowPos = 0;

    @Override
    protected void doProcess(DataHandlerResult result, BinlogEvent binlogEvent, PumaContext context, byte eventType) {

        switch (eventType) {
            case BinlogConstanst.TABLE_MAP_EVENT:
                TableMapEvent tableMapEvent = (TableMapEvent) binlogEvent;

                tableMetaInfo = getTableMetasInfoFetcher().getTableMetaInfo(tableMapEvent.getDatabaseName(),
                        tableMapEvent.getTableName());
                if (tableMetaInfo == null) {
                    skipEvent(result, context);
                    return;
                }
                fillRawTypeCodes(tableMapEvent, tableMetaInfo);
                fillRawNullAbilities(tableMapEvent, tableMetaInfo);

                rowPos = 0;

                result.setEmpty(true);
                result.setFinished(true);

                break;
            case BinlogConstanst.WRITE_ROWS_EVENT:
                if (tableMetaInfo == null) {
                    skipEvent(result, context);
                    return;
                }

                processWriteRowEvent(result, binlogEvent, context);
                break;

            case BinlogConstanst.UPDATE_ROWS_EVENT:
                if (tableMetaInfo == null) {
                    skipEvent(result, context);
                    return;
                }
                processUpdateRowEvent(result, binlogEvent, context);
                break;
            case BinlogConstanst.DELETE_ROWS_EVENT:
                if (tableMetaInfo == null) {
                    skipEvent(result, context);
                    return;
                }
                processDeleteRowEvent(result, binlogEvent, context);
                break;
            case BinlogConstanst.XID_EVENT:
                if (tableMetaInfo == null) {
                    skipEvent(result, context);
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
        dataChangedEvent.setDatabase(tableMetaInfo.getDatabase());

        result.setData(dataChangedEvent);
        result.setEmpty(false);
        result.setFinished(true);
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
            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(deleteRowsEvent, rowChangedEvent, RowChangedEvent.DELETE);

            for (int columnPos = 0, columnIndex = 0; columnPos < deleteRowsEvent.getColumnCount().intValue(); columnPos++) {
                if (deleteRowsEvent.getUsedColumns().get(columnPos)) {
                    Column binlogColumn = deleteRowsEvent.getRows().get(rowPos).getColumns().get(columnIndex);
                    String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                    checkUnknownColumnName(context, columnName, columnPos + 1);
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
            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(updateRowsEvent, rowChangedEvent, RowChangedEvent.UPDATE);

            for (int columnPos = 0, columnAfterIndex = 0, columnBeforeIndex = 0; columnPos < updateRowsEvent
                    .getColumnCount().intValue(); columnPos++) {
                String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                checkUnknownColumnName(context, columnName, columnPos + 1);
                Column afterColumn = null;
                Column beforeColumn = null;
                if (updateRowsEvent.getUsedColumnsAfter().get(columnPos)) {
                    afterColumn = updateRowsEvent.getRows().get(rowPos).getAfter().getColumns().get(columnAfterIndex);
                    columnAfterIndex++;
                }
                if (updateRowsEvent.getUsedColumnsBefore().get(columnPos)) {
                    beforeColumn = updateRowsEvent.getRows().get(rowPos).getBefore().getColumns()
                            .get(columnBeforeIndex);
                    columnBeforeIndex++;
                }
                ColumnInfo columnInfo = new ColumnInfo(tableMetaInfo.getKeys().contains(columnName),
                        beforeColumn == null ? null : convertUnsignedValueIfNeeded(columnPos + 1,
                                beforeColumn.getValue(), tableMetaInfo), afterColumn == null ? null
                                : convertUnsignedValueIfNeeded(columnPos + 1, afterColumn.getValue(), tableMetaInfo));
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
            RowChangedEvent rowChangedEvent = new RowChangedEvent();
            Map<String, ColumnInfo> columns = initColumns(writeRowsEvent, rowChangedEvent, RowChangedEvent.INSERT);

            for (int columnPos = 0, columnIndex = 0; columnPos < writeRowsEvent.getColumnCount().intValue(); columnPos++) {
                if (writeRowsEvent.getUsedColumns().get(columnPos)) {
                    Column binlogColumn = writeRowsEvent.getRows().get(rowPos).getColumns().get(columnIndex);
                    String columnName = tableMetaInfo.getColumns().get(columnPos + 1);
                    checkUnknownColumnName(context, columnName, columnPos + 1);
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

    protected void checkUnknownColumnName(PumaContext context, String columnName, int pos) {
        if (columnName == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unknown column for Binlog:  ").append(context.getBinlogFileName()).append(" BinlogPos: ")
                    .append(context.getBinlogStartPos()).append(" Skip to ").append(context.getNextBinlogPos());
            msg.append(" columnPos: ").append(pos);
            log.warn(msg.toString());
            if (getNotifyService() != null) {
                getNotifyService().alarm(msg.toString(), null, false);
            }
        }
    }

    protected void skipEvent(DataHandlerResult result, PumaContext context) {
        rowPos = 0;
        result.setEmpty(true);
        result.setFinished(true);
        StringBuilder msg = new StringBuilder();
        msg.append("Skip one event, since there is no table meta info. Binlog: ").append(context.getBinlogFileName())
                .append(" Pos: ").append(context.getBinlogStartPos()).append(" Skip to ")
                .append(context.getNextBinlogPos());
        log.warn(msg.toString());
        if (getNotifyService() != null) {
            getNotifyService().alarm(msg.toString(), null, false);
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
            int actionType) {
        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
        rowChangedData.setActionType(actionType);
        rowChangedData.setExecuteTime(rowsEvent.getHeader().getTimestamp());
        rowChangedData.setColumns(columns);
        rowChangedData.setDatabase(tableMetaInfo.getDatabase());
        rowChangedData.setTable(tableMetaInfo.getTable());
        return columns;
    }
}
