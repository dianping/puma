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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.puma.client.DataChangedEvent;
import com.dianping.puma.client.RowChangedData;
import com.dianping.puma.client.RowChangedData.ActionType;
import com.dianping.puma.client.RowChangedData.ColumnInfo;
import com.dianping.puma.client.TableChangedData;
import com.dianping.puma.client.TableMetaInfo;
import com.dianping.puma.common.annotation.ThreadUnSafe;
import com.dianping.puma.common.bo.PumaContext;
import com.dianping.puma.common.mysql.BinlogConstanst;
import com.dianping.puma.common.mysql.Row;
import com.dianping.puma.common.mysql.UpdatedRowData;
import com.dianping.puma.common.mysql.column.Column;
import com.dianping.puma.common.mysql.event.AbstractRowsEvent;
import com.dianping.puma.common.mysql.event.BinlogEvent;
import com.dianping.puma.common.mysql.event.DeleteRowsEvent;
import com.dianping.puma.common.mysql.event.TableMapEvent;
import com.dianping.puma.common.mysql.event.UpdateRowsEvent;
import com.dianping.puma.common.mysql.event.WriteRowsEvent;
import com.dianping.puma.common.mysql.event.XIDEvent;

/**
 * TODO Comment of TransactionSupportDataHandler
 * 
 * @author Leo Liang
 * 
 */
@ThreadUnSafe
public class TransactionSupportDataHandler extends AbstractDataHandler {
	private List<TableChangedData>					datas				= null;
	private Map<TableMetaInfo, TableChangedData>	tableChangedDataMap	= null;
	private TableChangedData						tableChangedData	= null;

	@Override
	protected DataChangedEvent doProcess(BinlogEvent binlogEvent, PumaContext context, byte eventType) {
		if (tableChangedDataMap == null) {
			tableChangedDataMap = new HashMap<TableMetaInfo, TableChangedData>();
		}
		if (datas == null) {
			datas = new ArrayList<TableChangedData>();
		}

		switch (eventType) {
			case BinlogConstanst.XID_EVENT:
				if (datas != null && !datas.isEmpty()) {
					DataChangedEvent dataChangedEvent = new DataChangedEvent();
					dataChangedEvent.setDatas(datas);
					dataChangedEvent.setTransactionId(((XIDEvent) binlogEvent).getXid());
					this.datas = null;
					this.tableChangedDataMap = null;
					this.tableChangedData = null;
					return dataChangedEvent;
				}
				break;
			case BinlogConstanst.TABLE_MAP_EVENT:
				TableMapEvent tableMapEvent = (TableMapEvent) binlogEvent;

				TableMetaInfo tableMeta = getTableMetaInfo(tableMapEvent.getDatabaseName(),
						tableMapEvent.getTableName());
				tableChangedData = tableChangedDataMap.get(tableMeta);
				fillRawTypeCodes(tableMapEvent, tableMeta);
				fillRawNullAbilities(tableMapEvent, tableMeta);

				if (tableChangedData == null) {
					TableChangedData tableChangedData = new TableChangedData();
					tableChangedData.setMeta(tableMeta);
					this.tableChangedData = tableChangedData;
					tableChangedDataMap.put(tableMeta, tableChangedData);
					datas.add(tableChangedData);
				}

				break;
			case BinlogConstanst.WRITE_ROWS_EVENT:
				WriteRowsEvent writeRowsEvent = (WriteRowsEvent) binlogEvent;

				List<RowChangedData> writeRowChangedDataList = new ArrayList<RowChangedData>();
				for (Row row : writeRowsEvent.getRows()) {
					RowChangedData rowChangedData = new RowChangedData();
					Map<String, ColumnInfo> columns = initColumns(writeRowsEvent, writeRowChangedDataList,
							rowChangedData, ActionType.INSERT);

					for (int columnPos = 0; columnPos < writeRowsEvent.getUsedColumns().size(); columnPos++) {
						if (writeRowsEvent.getUsedColumns().get(columnPos)) {
							Column binlogColumn = row.getColumns().get(columnPos);
							String columnName = this.tableChangedData.getMeta().getColumns().get(columnPos + 1);
							ColumnInfo columnInfo = new ColumnInfo(this.tableChangedData.getMeta().getTypes()
									.get(columnName), null, binlogColumn.getValue(), columnPos + 1, columnName);
							columns.put(columnName, columnInfo);
						}
					}
				}

				this.tableChangedData.setRows(writeRowChangedDataList);

				break;
			case BinlogConstanst.UPDATE_ROWS_EVENT:
				UpdateRowsEvent updateRowsEvent = (UpdateRowsEvent) binlogEvent;

				List<RowChangedData> updateRowChangedDataList = new ArrayList<RowChangedData>();
				for (UpdatedRowData<Row> row : updateRowsEvent.getRows()) {
					RowChangedData rowChangedData = new RowChangedData();
					Map<String, ColumnInfo> columns = initColumns(updateRowsEvent, updateRowChangedDataList,
							rowChangedData, ActionType.UPDATE);

					for (int columnBeforePos = 0; columnBeforePos < updateRowsEvent.getUsedColumnsBefore().size(); columnBeforePos++) {
						if (updateRowsEvent.getUsedColumnsBefore().get(columnBeforePos)) {
							Column binlogColumnBefore = row.getBefore().getColumns().get(columnBeforePos);
							String columnName = this.tableChangedData.getMeta().getColumns().get(columnBeforePos + 1);
							ColumnInfo columnInfo = new ColumnInfo(this.tableChangedData.getMeta().getTypes()
									.get(columnName), binlogColumnBefore.getValue(), null, columnBeforePos + 1,
									columnName);
							columns.put(columnName, columnInfo);
						}
					}
					for (int columnAfterPos = 0; columnAfterPos < updateRowsEvent.getUsedColumnsAfter().size(); columnAfterPos++) {
						if (updateRowsEvent.getUsedColumnsAfter().get(columnAfterPos)) {
							Column binlogColumnAfter = row.getAfter().getColumns().get(columnAfterPos);
							String columnName = this.tableChangedData.getMeta().getColumns().get(columnAfterPos + 1);
							ColumnInfo columnInfo = columns.get(columnName);
							if (columnInfo == null) {
								columnInfo = new ColumnInfo(this.tableChangedData.getMeta().getTypes().get(columnName),
										null, binlogColumnAfter.getValue(), columnAfterPos + 1, columnName);
								columns.put(columnName, columnInfo);
							} else {
								columnInfo.setNewValue(binlogColumnAfter.getValue());
							}
						}
					}
				}

				this.tableChangedData.setRows(updateRowChangedDataList);
				break;
			case BinlogConstanst.DELETE_ROWS_EVENT:
				DeleteRowsEvent deleteRowsEvent = (DeleteRowsEvent) binlogEvent;

				List<RowChangedData> deleteRowChangedDataList = new ArrayList<RowChangedData>();
				for (Row row : deleteRowsEvent.getRows()) {
					RowChangedData rowChangedData = new RowChangedData();
					Map<String, ColumnInfo> columns = initColumns(deleteRowsEvent, deleteRowChangedDataList,
							rowChangedData, ActionType.DELETE);

					for (int columnPos = 0; columnPos < deleteRowsEvent.getUsedColumns().size(); columnPos++) {
						if (deleteRowsEvent.getUsedColumns().get(columnPos)) {
							Column binlogColumn = row.getColumns().get(columnPos);
							String columnName = this.tableChangedData.getMeta().getColumns().get(columnPos + 1);
							ColumnInfo columnInfo = new ColumnInfo(this.tableChangedData.getMeta().getTypes()
									.get(columnName), binlogColumn.getValue(), null, columnPos + 1, columnName);
							columns.put(columnName, columnInfo);
						}
					}
				}

				this.tableChangedData.setRows(deleteRowChangedDataList);
				break;
			default:
				break;
		}

		return null;

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

	private Map<String, ColumnInfo> initColumns(AbstractRowsEvent rowsEvent,
			List<RowChangedData> writeRowChangedDataList, RowChangedData rowChangedData, ActionType actionType) {
		Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
		rowChangedData.setActionType(actionType);
		rowChangedData.setExecuteTime(rowsEvent.getHeader().getTimestamp());
		rowChangedData.setColumns(columns);
		writeRowChangedDataList.add(rowChangedData);
		return columns;
	}
}
