package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.api.PumaServerRouter;
import com.dianping.puma.api.cleanup.CleanUp;
import com.dianping.puma.api.cleanup.CleanUpHelper;
import com.dianping.puma.api.lock.PumaClientLock;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClient implements PumaClient {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterPumaClient.class);

    private final String clientName;

    private final String database;

    private final List<String> tables;

    private final boolean dml;

    private final boolean ddl;

    private final boolean transaction;

    private final PumaClientLock lock;

    private final PumaServerRouter router;

    protected int retryTimes = 3; // 3 times.

    protected int retryInterval = 5000; // 5s.

    protected SimplePumaClient client;

    public ClusterPumaClient(PumaClientConfig config) {
        this.clientName = config.getClientName();
        this.database = config.getDatabase();
        this.tables = config.getTables();
        this.dml = config.isDml();
        this.ddl = config.isDdl();
        this.transaction = config.isTransaction();
        this.router = config.getRouter();
        this.lock = new PumaClientLock(config.getClientName());
        CleanUpHelper.register(this, new ClusterPumaClientCleanUp(this.lock));
    }

    protected SimplePumaClient newClient() throws PumaClientException {
        String serverHost = router.next();
        if (serverHost == null) {
            String msg = String.format("[%s] failed to create new client. No puma server available.", clientName);
            PumaClientException e = new PumaClientException(msg);
            LOG.error(msg, e);
        }

        return new PumaClientConfig()
                .setClientName(clientName)
                .setDatabase(database)
                .setTables(tables)
                .setDml(dml)
                .setDdl(ddl)
                .setTransaction(transaction)
                .setServerHost(serverHost)
                .buildSimplePumaClient();
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException {
        return get(batchSize, 0, null);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        lock();

        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;
        for (int i = 0; i <= retryTimes; ++i) {
            try {
                return client.get(batchSize, timeout, timeUnit);
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                LOG.warn("failed to get binlog message from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to get after %s times retries.", clientName, retryTimes);
        LOG.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        BinlogMessage message = get(batchSize, timeout, timeUnit);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
        BinlogMessage message = get(batchSize);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public void ack(BinlogInfo binlogInfo) throws PumaClientException {
        lock();

        if (needNewClient()) {
            client = newClient();
        }
        Exception lastException = null;

        for (int i = 0; i <= retryTimes; ++i) {
            try {
                client.ack(binlogInfo);
                return;
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                LOG.warn("failed to ack binlog position to server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to ack after %s times retries.", clientName, retryTimes);
        LOG.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
        lock();

        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;

        for (int i = 0; i <= retryTimes; ++i) {
            try {
                client.rollback(binlogInfo);
                return;
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                LOG.warn("failed to rollback binlog message from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to rollback after %s times retries.", clientName, retryTimes);
        LOG.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    protected boolean needNewClient() {
        return client == null || !router.exist(client.getServerHost());
    }

    protected void lock() throws PumaClientException {
        while (!Thread.interrupted())
            try {
                if (lock.lock(10, TimeUnit.SECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PumaClientException("Thread has been interrupted!", e);
            } catch (Exception e) {
                throw new PumaClientException("Lock failed!", e);
            }
    }

    static class ClusterPumaClientCleanUp implements CleanUp {
        private final PumaClientLock lock;

        public ClusterPumaClientCleanUp(PumaClientLock lock) {
            this.lock = lock;
        }

        @Override
        public void cleanUp() {
            try {
                lock.unlock();
            } catch (Exception ignore) {
            }
        }
    }
}
