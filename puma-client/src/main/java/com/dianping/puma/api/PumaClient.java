package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientAuthException;
import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogMessage;

import java.util.concurrent.TimeUnit;

public interface PumaClient {

    void connect() throws PumaClientException;

    void disconnect() throws PumaClientException;

    BinlogMessage get(int batchSize) throws PumaClientException, PumaClientAuthException;

    BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, PumaClientAuthException;

    BinlogMessage getWithAck(int batchSize) throws PumaClientException, PumaClientAuthException;

    BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, PumaClientAuthException;

    void ack(BinlogInfo binlogInfo) throws PumaClientException, PumaClientAuthException;

    void rollback(BinlogInfo binlogInfo) throws PumaClientException, PumaClientAuthException;

    void rollback() throws PumaClientException, PumaClientAuthException;

    void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) throws PumaClientException;
}
