package com.dianping.puma.api;

import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;

import java.util.concurrent.TimeUnit;

public interface PumaConnector {

    void connect() throws PumaClientException;

    void disconnect() throws PumaClientException;

    BinlogMessage get(int batchSize) throws PumaClientException;

    BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    BinlogMessage getWithAck(int batchSize) throws PumaClientException;

    BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    void ack(long batchId) throws PumaClientException;

    void rollback(long batchId) throws PumaClientException;

    void rollback() throws PumaClientException;

    void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) throws PumaClientException;
}
