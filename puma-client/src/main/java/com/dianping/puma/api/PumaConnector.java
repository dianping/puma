package com.dianping.puma.api;

import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;

import java.util.concurrent.TimeUnit;

public interface PumaConnector {

	void connect() throws PumaClientException;

	void disconnect() throws PumaClientException;

	BinlogMessage get(int batchSize) throws PumaClientException;

	BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

	BinlogMessage getWithoutAck(int batchSize) throws PumaClientException;

	BinlogMessage getWithoutAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

	void ack(long batchId) throws PumaClientException;

	void rollback(long batchId) throws PumaClientException;

	void rollback() throws PumaClientException;
}
