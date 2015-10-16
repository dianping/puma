package com.dianping.puma.api.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ZkPumaClientLock implements PumaClientLock {

    private static final Logger LOG = LoggerFactory.getLogger(ZkPumaClientLock.class);

    private volatile boolean lockState = false;

    private final InterProcessSemaphoreMutex lock;

    private final String clientName;

    public ZkPumaClientLock(final String clientName) {
        CuratorFramework zkClient = new LionZkManager().getZkClient();

        this.clientName = clientName;
        this.lock = new InterProcessSemaphoreMutex(zkClient, genLockPath(clientName));

        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState.equals(ConnectionState.LOST) || newState.equals(ConnectionState.SUSPENDED)) {
                    LOG.info("zookeeper connection lost or suspend for lock `{}`.", clientName);
                    lockState = false;
                }
            }
        });
    }

    @Override
    public void lock() throws Exception {
        if (!lockState) {
            lock.acquire();
            lockState = true;
            LOG.info("{} get the lock", clientName);
        }
    }

    @Override
    public boolean lock(long time, TimeUnit timeUnit) throws Exception {
        if (!lockState) {
            lockState = lock.acquire(time, timeUnit);
            if (lockState) {
                LOG.info("{} get the lock", clientName);
            }
            return lockState;
        } else {
            return true;
        }
    }

    @Override
    public void unlock() throws Exception {
        if(lockState) {
            lock.release();
            lockState = false;
            LOG.info("{} release the lock", clientName);
        }
    }

    protected String genLockPath(String lockName) {
        return "/dp/lock/puma/" + lockName;
    }
}
