package com.dianping.puma.api;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PumaClientMainTest {

	public PumaClient createPumaClient() {

		final PumaClient pumaClient = new PumaClient();
		pumaClient.setName("test");
		pumaClient.setDatabase("test-database");
		List<String> tables = new ArrayList<String>();
		tables.add("test-table0");
		tables.add("test-table1");
		pumaClient.setTables(tables);

		pumaClient.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) {
				//System.out.println("Bingo!");

				if (event instanceof RowChangedEvent) {
					// UPDATE, INSERT, DELETE

					RowChangedEvent rowChangedEvent = (RowChangedEvent) event;

					// UPDATE.
					if (rowChangedEvent.getDmlType() == DMLType.UPDATE) {
						System.out.println("UPDATE");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry : columnInfoMap.entrySet()) {
							System.out.println("Column Name: " + entry.getKey());
							System.out.println("Column value before update: " + entry.getValue().getOldValue());
							System.out.println("Column value after update: " + entry.getValue().getNewValue());
						}
					}

					// INSERT.
					if (rowChangedEvent.getDmlType() == DMLType.INSERT) {
						System.out.println("INSERT");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry : columnInfoMap.entrySet()) {
							System.out.println("Column Name: " + entry.getKey());
							System.out.println("Column value inserted: " + entry.getValue().getNewValue());
						}
					}

					// DELETE.
					if (rowChangedEvent.getDmlType() == DMLType.DELETE) {
						System.out.println("DELETE");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry : columnInfoMap.entrySet()) {
							System.out.println("Column Name: " + entry.getKey());
							System.out.println("Column value deleted: " + entry.getValue().getOldValue());
						}
					}
				}
			}

		});

		return pumaClient;
	}
	
	public static void main(String []args){
		PumaClientMainTest main = new PumaClientMainTest();
		PumaClient pumaClient = main.createPumaClient();
		pumaClient.start();
	}
}
