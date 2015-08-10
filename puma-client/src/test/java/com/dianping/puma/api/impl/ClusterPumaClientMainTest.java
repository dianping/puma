package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;

import java.util.ArrayList;
import java.util.List;

public class ClusterPumaClientMainTest {

	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>();
		tables.add("puma-test-tb");

		PumaClient client = new PumaClientConfig()
				.setClientName("puma-test")
				.setDatabase("test-database")
				.setTables(tables)
				.buildClusterPumaClient();

		client.get(10);
	}
}
