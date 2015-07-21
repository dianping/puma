package com.dianping.puma.api.router;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RemotePumaServerRouterTest {

	RemotePumaServerRouter remotePumaServerRouter = new RemotePumaServerRouter();

	@Test
	public void testGenUrl() throws Exception {
		remotePumaServerRouter.remotePumaServerRouterHost = "test-host";

		List<String> tables = new ArrayList<String>();
		tables.add("test-tb0");
		tables.add("test-tb1");
		String result = remotePumaServerRouter.genUrl("test-db", tables);

		assertEquals("test-host?database=test-db&tables=test-tb0&tables=test-tb1", result);
	}
}