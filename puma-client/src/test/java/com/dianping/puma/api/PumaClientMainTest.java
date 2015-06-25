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
				System.out.println(event);
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
