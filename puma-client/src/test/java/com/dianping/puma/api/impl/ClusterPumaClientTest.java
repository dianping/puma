package com.dianping.puma.api.impl;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.core.dto.BinlogMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClusterPumaClientTest extends MockTest {

	@Spy
	ClusterPumaClient client;

	@Mock
	SimplePumaClient simpleClient;

	@Mock
	Logger logger;

	@Before
	public void before() {
		reset(client);
		reset(simpleClient);
	}

	@Test
	public void testGetSuccess() {
		doNothing().when(client).restart(anyString(), anyList());
		doReturn(new BinlogMessage()).when(simpleClient).get(anyInt());
		client.currentPumaClient = simpleClient;
		client.subscribed = true;
		client.retryTimes = 3;

		client.get(10);
		verify(client, times(0)).restart(anyString(), anyList());
		verify(client, times(1)).get(10);
	}

	@Test(expected = RuntimeException.class)
	public void testGetFailure() {
		doNothing().when(client).restart(anyString(), anyList());
		doThrow(new RuntimeException("mock exception")).when(simpleClient).get(anyInt());
		client.currentPumaClient = simpleClient;
		client.subscribed = true;
		client.retryTimes = 3;

		try {
			client.get(10);
		} catch (Exception e) {
			verify(client, times(3)).restart(anyString(), anyList());
			throw new RuntimeException();
		}
	}
}