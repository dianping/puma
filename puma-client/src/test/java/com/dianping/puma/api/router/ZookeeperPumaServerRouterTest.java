package com.dianping.puma.api.router;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ZookeeperPumaServerRouterTest {

	ZookeeperPumaServerRouter router = new ZookeeperPumaServerRouter();

	@Test
	public void testParseZkPath() {
		String database = "test-database";
		List<String> tables = new ArrayList<String>();
		tables.add("test-table0");
		tables.add("test-table1");

		String expected = "puma.client.route.test-database";
		String result = router.parseZkPath(database, tables);

		assertEquals(expected, result);
	}

	@Test
	public void testParsePumaServers() {
		String str = "1.1.1.1:8080#0.1\t0.0.0.0:4040#0.95";

		Map<String, Double> expected = new HashMap<String, Double>();
		expected.put("1.1.1.1:8080", 0.1);
		expected.put("0.0.0.0:4040", 0.95);
		Map<String, Double> result = router.parsePumaServers(str);

		assertEquals(expected, result);
	}
}