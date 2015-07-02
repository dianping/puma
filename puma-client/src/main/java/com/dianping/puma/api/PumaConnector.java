package com.dianping.puma.api;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;

import java.util.concurrent.TimeUnit;

public interface PumaConnector {

    void connect() throws PumaClientException;

    void disconnect() throws PumaClientException;

    BinlogMessage get(int batchSize) throws PumaClientException, InterruptedException;

    BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, InterruptedException;

    BinlogMessage getWithAck(int batchSize) throws PumaClientException, InterruptedException;

    BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, InterruptedException;

    void ack(BinlogInfo binlogInfo) throws PumaClientException, InterruptedException;

    void rollback(BinlogInfo binlogInfo) throws PumaClientException;

    void rollback() throws PumaClientException;

    void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) throws PumaClientException;
}
