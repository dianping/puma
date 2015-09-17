package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.api.PumaServerRouter;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.lock.DistributedLock;
import com.dianping.puma.core.lock.DistributedLockFactory;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClient implements PumaClient {

    private static final Logger logger = LoggerFactory.getLogger(ClusterPumaClient.class);

    private String clientName;

    private String database;

    private List<String> tables;

    private boolean dml;

    private boolean ddl;

    private boolean transaction;

    private PumaServerRouter router;

    protected int retryTimes = 3; // 3 times.

    protected int retryInterval = 5000; // 5s.

    protected volatile SimplePumaClient client;

    private DistributedLock distributedLock;

    public ClusterPumaClient() {
    }

    public ClusterPumaClient(PumaClientConfig config) {
        clientName = config.getClientName();
        database = config.getDatabase();
        tables = config.getTables();
        dml = config.isDml();
        ddl = config.isDdl();
        transaction = config.isTransaction();
        router = config.getRouter();
    }

    protected SimplePumaClient newClient() {
        String serverHost = router.next();
        if (serverHost == null) {
            String msg = String.format("[%s] failed to create new client.", clientName);
            RuntimeException e = new RuntimeException("no puma server available.");
            logger.error(msg, e);
            throw new PumaClientException(msg, e);
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
        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;
        for (int i = 0; i <= retryTimes; ++i) {
            try {
                return client.get(batchSize);
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                logger.warn("[{}] failed to get binlog message from server({}).\n{}",
                        new Object[]{clientName, client.getServerHost(), ExceptionUtils.getStackTrace(t)});

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to get after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
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

                logger.warn("failed to get binlog message from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to get after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;
        for (int i = 0; i <= retryTimes; ++i) {
            try {
                return client.getWithAck(batchSize, timeout, timeUnit);
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                logger.warn("failed to get binlog message with ack from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to get with ack after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;
        for (int i = 0; i <= retryTimes; ++i) {
            try {
                return client.getWithAck(batchSize);
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                logger.warn("failed to get binlog message with ack from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to get with ack after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public void ack(BinlogInfo binlogInfo) throws PumaClientException {
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

                logger.warn("failed to ack binlog position to server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to ack after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public void rollback() throws PumaClientException {
        if (needNewClient()) {
            client = newClient();
        }

        Exception lastException = null;
        for (int i = 0; i <= retryTimes; ++i) {
            try {
                client.rollback();
                return;
            } catch (Exception t) {
                lastException = t;
                if (i == retryTimes) {
                    break;
                }

                logger.warn("failed to rollback binlog message from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to rollback after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
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

                logger.warn("failed to rollback binlog message from server {}.\n{}",
                        client.getServerHost(),
                        ExceptionUtils.getStackTrace(t));

                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
                client = newClient();
            }
        }

        String msg = String.format("[%s] failed to rollback after %s times retries.", clientName, retryTimes);
        logger.error(msg);
        throw new PumaClientException(msg, lastException);
    }

    protected boolean needNewClient() {
        if (client == null) {
            return true;
        }

        return !router.exist(client.getServerHost());
    }
}
