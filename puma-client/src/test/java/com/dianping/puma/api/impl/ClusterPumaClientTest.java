package com.dianping.puma.api.impl;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.junit.Assert.assertEquals;

public class ClusterPumaClientTest extends MockTest {

	@Spy
	ClusterPumaClient clusterPumaClient;

	@Mock
	SimplePumaClient simplePumaClient;

	@Before
	public void before() {
		reset(clusterPumaClient, simplePumaClient);
	}

	@Test
	public void testGetFirst() {
		clusterPumaClient.client = null;
		doReturn(simplePumaClient).when(clusterPumaClient).newClient();
		BinlogMessage binlogMessage = new BinlogMessage();
		doReturn(binlogMessage).when(simplePumaClient).get(anyInt());

		assertEquals(binlogMessage, clusterPumaClient.get(100));
		verify(clusterPumaClient, times(1)).newClient();
		verify(simplePumaClient, times(1)).get(100);

		assertEquals(binlogMessage, clusterPumaClient.get(1000));
		verify(clusterPumaClient, times(1)).newClient();
		verify(simplePumaClient, times(1)).get(1000);
	}

	@SuppressWarnings("unchecked")
   @Test
	public void testGetSuccess() {
		clusterPumaClient.client = simplePumaClient;
		BinlogMessage binlogMessage = new BinlogMessage();
		doReturn(binlogMessage).when(simplePumaClient).get(anyInt());

		assertEquals(binlogMessage, clusterPumaClient.get(1));
		verify(clusterPumaClient, times(0)).newClient();
		verify(simplePumaClient, times(1)).get(1);

		assertEquals(binlogMessage, clusterPumaClient.get(10));
		verify(clusterPumaClient, times(0)).newClient();
		verify(simplePumaClient, times(1)).get(10);
	}

	@Test(expected = PumaClientException.class)
	public void testGetFailure() {
		clusterPumaClient.client = simplePumaClient;
		clusterPumaClient.retryTimes = 5;
		doReturn(simplePumaClient).when(clusterPumaClient).newClient();
		doThrow(new PumaClientException()).when(simplePumaClient).get(anyInt());

		try {
			clusterPumaClient.get(20);
		} catch (PumaClientException e) {
			throw e;
		} finally {
			verify(clusterPumaClient, times(5)).newClient();
			verify(simplePumaClient, times(6)).get(20);
		}
	}
}