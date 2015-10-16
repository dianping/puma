package com.dianping.puma.api.impl;

import com.dianping.puma.api.MockTest;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.api.lock.PumaClientLock;
import com.dianping.puma.core.dto.BinlogMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class ClusterPumaClientTest extends MockTest {


    @Spy
    ClusterPumaClient clusterPumaClient = new ClusterPumaClient(new PumaClientConfig().setEnableEventLog(true));

    @Mock
    SimplePumaClient simplePumaClient;

    @Before
    public void before() throws PumaClientException {
        reset(clusterPumaClient, simplePumaClient);
    }

    @Test
    public void testAutoCreateNewClient() throws PumaClientException {
        doNothing().when(clusterPumaClient).lock();

        doReturn(simplePumaClient).when(clusterPumaClient).newClient();

        BinlogMessage binlogMessage = new BinlogMessage();
        doReturn(binlogMessage).when(simplePumaClient).get(anyInt(), anyLong(), any(TimeUnit.class));

        doReturn(true).when(clusterPumaClient).needNewClient();

        assertEquals(binlogMessage, clusterPumaClient.get(100));
        verify(clusterPumaClient, times(1)).newClient();
        verify(simplePumaClient, times(1)).get(100, 0, null);

        doReturn(false).when(clusterPumaClient).needNewClient();

        assertEquals(binlogMessage, clusterPumaClient.get(1000));
        verify(clusterPumaClient, times(1)).newClient();
        verify(simplePumaClient, times(1)).get(1000, 0, null);
    }

    @Test(expected = PumaClientException.class)
    public void testGetFailure() throws PumaClientException {
        doNothing().when(clusterPumaClient).lock();

        clusterPumaClient.client = simplePumaClient;
        clusterPumaClient.retryTimes = 3;
        clusterPumaClient.retryInterval = 1;
        doReturn(simplePumaClient).when(clusterPumaClient).newClient();
        doReturn(false).when(clusterPumaClient).needNewClient();

        doThrow(new PumaClientException()).when(simplePumaClient).get(anyInt(), anyLong(), any(TimeUnit.class));

        try {
            clusterPumaClient.get(20);
        } catch (PumaClientException e) {
            throw e;
        } finally {
            verify(clusterPumaClient, times(3)).newClient();
            verify(simplePumaClient, times(4)).get(20, 0, null);
        }
    }

    @Test
    public void testLockCanBeInterapted() throws Exception {
        final MockLock lock = new MockLock();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ClusterPumaClient client = new ClusterPumaClient(new PumaClientConfig().setEnableEventLog(true), lock);
                while (true) {
                    try {
                        client.lock();
                    } catch (PumaClientException e) {
                        System.out.println("interrupt");
                        break;
                    }
                }
            }
        });

        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        Thread.sleep(2000);

        assert thread.getState().equals(Thread.State.TERMINATED);
    }

    @Test
    public void testLockSuccess() throws Exception {
        final MockLock lock = new MockLock();

        final AtomicReference<Exception> exp = new AtomicReference<Exception>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ClusterPumaClient client = new ClusterPumaClient(new PumaClientConfig().setEnableEventLog(true), lock);
                try {
                    client.lock();
                } catch (PumaClientException e) {
                    exp.set(e);
                }
            }
        });

        thread.start();
        Thread.sleep(100);
        lock.setLockSuccess(true);
        Thread.sleep(1200);

        assert thread.getState().equals(Thread.State.TERMINATED);
        assert exp.get() == null;
    }

    static class MockLock implements PumaClientLock {
        private volatile boolean lockSuccess;

        @Override
        public void lock() throws Exception {

        }

        @Override
        public boolean lock(long time, TimeUnit timeUnit) throws Exception {
            timeUnit.sleep(time);
            return lockSuccess;
        }

        @Override
        public void unlock() throws Exception {

        }

        public void setLockSuccess(boolean lockSuccess) {
            this.lockSuccess = lockSuccess;
        }
    }
}