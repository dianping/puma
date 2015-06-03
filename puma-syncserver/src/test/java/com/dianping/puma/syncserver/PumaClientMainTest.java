package com.dianping.puma.syncserver;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.ConfigurationBuilder;
import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;

import java.util.Map;

public class PumaClientMainTest {

	public PumaClient createPumaClient() {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();

		// Set puma client target.
		configBuilder.target("DPShop@puma01_nh");

		// Set puma client name.
		configBuilder.name("YourClientName");

		// Set the database and tables you want to listen to on format:
		// "database", "table_1", "table_2", ...
		configBuilder.tables("DPShop", "DP_Shop","DP_ShopPOI");

		Configuration configuration = configBuilder.build();

		System.out.println("Configuration: " + configuration.toString());

		final PumaClient pumaClient = new PumaClient(configuration);

		pumaClient.register(new EventListener() {

			@Override
			public void onEvent(ChangedEvent event) throws Exception {
				//System.out.println("Bingo!");

				if (event instanceof RowChangedEvent) {
					// UPDATE, INSERT, DELETE

					RowChangedEvent rowChangedEvent = (RowChangedEvent) event;

					// UPDATE.
					if (rowChangedEvent.getDmlType() == DMLType.UPDATE) {
						System.out.println("UPDATE");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry: columnInfoMap.entrySet()) {
							//System.out.println("Column Name: " + entry.getKey());
							//System.out.println("Column value before update: " + entry.getValue().getOldValue());
							//System.out.println("Column value after update: " + entry.getValue().getNewValue());
						}
					}

					// INSERT.
					if (rowChangedEvent.getDmlType() == DMLType.INSERT) {
						System.out.println("INSERT");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry: columnInfoMap.entrySet()) {
							//System.out.println("Column Name: " + entry.getKey());
							//System.out.println("Column value inserted: " + entry.getValue().getNewValue());
						}
					}

					// DELETE.
					if (rowChangedEvent.getDmlType() == DMLType.DELETE) {
						System.out.println("DELETE");

						Map<String, RowChangedEvent.ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
						for (Map.Entry<String, RowChangedEvent.ColumnInfo> entry: columnInfoMap.entrySet()) {
							//System.out.println("Column Name: " + entry.getKey());
							//System.out.println("Column value deleted: " + entry.getValue().getOldValue());
						}
					}
				}
			}

			@Override
			public boolean onException(ChangedEvent event, Exception e) {

				// Do your own exception handling.

				// Return false if you want to stop the puma client when exception is thrown from `onEvent`.
				// Return true if you want to ignore the exception and keep on listening.
				return false;
			}

			@Override
			public void onConnectException(Exception e) {
			}

			@Override
			public void onConnected() {
			}

			@Override
			public void onSkipEvent(ChangedEvent event) {
			}
		});

		return pumaClient;
	}
	
	public static void main(String []args){
		PumaClientMainTest main = new PumaClientMainTest();
		final PumaClient pumaClient = main.createPumaClient();
		pumaClient.start();

		Thread stopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					System.out.println("Stop client.");
					pumaClient.stop();
					System.out.println("Start client");
					pumaClient.start();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		stopThread.start();
	}
}
