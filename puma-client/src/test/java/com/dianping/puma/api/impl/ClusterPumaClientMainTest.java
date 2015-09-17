package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.*;
import com.dianping.puma.core.util.sql.DMLType;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dianping.puma.core.event.RowChangedEvent.*;

public class ClusterPumaClientMainTest {

	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>();
		tables.add("UOD_Order0");
		tables.add("UOD_Order1");
		tables.add("UOD_OrderExtraFields0");
		tables.add("UOD_OrderExtraFields1");
		tables.add("UOD_OrderLog0");
		tables.add("UOD_OrderLog1");
		tables.add("UOD_OrderPaymentDetail0");
		tables.add("UOD_OrderPaymentDetail1");
		tables.add("UOD_OrderSKU0");
		tables.add("UOD_OrderSKU1");
		tables.add("UOD_OrderSKUExtraFields0");
		tables.add("UOD_OrderSKUExtraFields1");

		boolean ddl = false;
		boolean dml = true;
		boolean transaction = false;
		boolean insert = true;
		boolean update = true;
		boolean delete = true;

		PumaClient client = new PumaClientConfig()
				.setClientName("dozer-debug")
				.setDatabase("UnifiedOrder0")
				.setTables(tables)
				.setDdl(true)
				.setDml(true)
				.setTransaction(true)
						//                .buildClusterPumaClient();
				.setServerHosts(Lists.newArrayList("127.0.0.1:4040"))
				.buildFixedClusterPumaClient();

		final int size = 100;

		while (true) {
			try {
				BinlogMessage message = client.get(size, 1, TimeUnit.SECONDS);

				for (Event event : message.getBinlogEvents()) {

					if (event instanceof DdlEvent) {
						if (!ddl) {
							continue;
						}

						// do ddl business logic here.
					} else if (event instanceof RowChangedEvent) {
						RowChangedEvent rowChangedEvent = (RowChangedEvent) event;

						if (rowChangedEvent.isTransactionBegin() || rowChangedEvent.isTransactionCommit()) {
							if (!transaction) {
								continue;
							}

							// do transaction business logic here.

						} else {
							if (!dml) {
								continue;
							}

							if (rowChangedEvent.getDmlType().equals(DMLType.INSERT)) {
								if (!insert) {
									continue;
								}

								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object insertValue = columnInfo.getNewValue();

									// do insert business logic here.
								}
							} else if (rowChangedEvent.getDmlType().equals(DMLType.UPDATE)) {
								if (!update) {
									continue;
								}

								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object oldValue = columnInfo.getOldValue();
									Object newValue = columnInfo.getNewValue();

									// do update business logic here.
								}
							} else if (rowChangedEvent.getDmlType().equals(DMLType.DELETE)) {
								if (!delete) {
									continue;
								}

								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object deleteValue = columnInfo.getOldValue();

									// do delete business logic here.
								}
							}

						}

					}

					System.out.println(event.toString());
				}

				client.ack(message.getLastBinlogInfo());
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}
}
