package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface PumaClient {

    String getPumaServerHost();

    BinlogMessage get(int batchSize) throws PumaClientException;

    BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    BinlogMessage getWithAck(int batchSize) throws PumaClientException;

    BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    void ack(BinlogInfo binlogInfo) throws PumaClientException;

    void rollback(BinlogInfo binlogInfo) throws PumaClientException;

    void rollback() throws PumaClientException;

    void subscribe(String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) throws PumaClientException;

    void unSubscribe() throws PumaClientException;
}
