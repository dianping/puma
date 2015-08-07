package com.dianping.puma.api.impl;

import com.dianping.puma.api.*;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClient implements PumaClient {

	private static final Logger logger = LoggerFactory.getLogger(ClusterPumaClient.class);

	private String name;

	private String database;

	private List<String> tables;

	private boolean dml = true; // default only need dml.

	private boolean ddl = false;

	private boolean transaction = false;

	protected int retryTimes = 3; // puma client ha retry times.

	protected int retryInterval = 5000;

	private PumaServerRouter router;

	protected volatile boolean subscribed = false;

	protected volatile PumaClient currentPumaClient;

	public ClusterPumaClient(String name, PumaServerRouter router) {
		this.name = name;
		this.router = router;
	}

	protected void start(String database, List<String> tables) {
		String currentHost = null;
		try {
			currentHost = router.next(database, tables);
			currentPumaClient = PumaClientFactory.createSimplePumaClient(name, currentHost);
		} catch (Throwable t) {
			throw new PumaClientException("fail to start puma client from server " + currentHost + ".", t);
		}
	}

	protected void restart(String database, List<String> tables) {
		String currentHost = null;
		try {
			currentHost = router.next(database, tables);
		} catch (Throwable t) {
			logger.warn("fail to route puma servers.\n{}", ExceptionUtils.getStackTrace(t));
		}

		try {
			Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);

			currentPumaClient = PumaClientFactory.createSimplePumaClient(name, currentHost);
			currentPumaClient.subscribe(database, tables, dml, ddl, transaction);
		} catch (Throwable t) {
			throw new PumaClientException("fail to restart puma client from server " + currentHost + ".", t);
		}
	}

	@Override
	public String getPumaServerHost() {
		if (currentPumaClient == null) {
			return null;
		}

		return currentPumaClient.getPumaServerHost();
	}

	@Override
	public BinlogMessage get(int batchSize) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("subscribe before you get binlog message.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				return currentPumaClient.get(batchSize);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message from server({}).\n{}", currentPumaClient.getPumaServerHost(),
				      ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}

		throw new PumaClientException("failed to get binlog message after " + retryTimes + " times retries.");
	}

	@Override
	public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				return currentPumaClient.get(batchSize, timeout, timeUnit);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message from server {}.\n{}", currentPumaClient.getPumaServerHost(),
				      ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}

		throw new PumaClientException("failed to get binlog message after " + retryTimes + " time retries.");
	}

	@Override
	public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message with ack, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				return currentPumaClient.getWithAck(batchSize, timeout, timeUnit);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message with ack from server {}.\n{}",
				      currentPumaClient.getPumaServerHost(), ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}

		throw new PumaClientException("failed to get binlog message with ack after " + retryTimes + " time retries.");
	}

	@Override
	public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message with ack, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				return currentPumaClient.getWithAck(batchSize);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message with ack from server {}.\n{}",
				      currentPumaClient.getPumaServerHost(), ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}

		throw new PumaClientException("failed to get binlog message with ack after " + retryTimes + " time retries.");
	}

	@Override
	public void ack(BinlogInfo binlogInfo) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to ack binlog position, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.ack(binlogInfo);
				return;
			} catch (Throwable t) {
				logger.warn("failed to ack binlog position to server {}.\n{}", currentPumaClient.getPumaServerHost(),
				      ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}
	}

	@Override
	public void rollback() throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to rollback binlog message, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.rollback();
				return;
			} catch (Throwable t) {
				logger.warn("failed to rollback binlog message from server {}.\n{}", currentPumaClient.getPumaServerHost(),
				      ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}
	}

	@Override
	public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to rollback binlog message, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.rollback(binlogInfo);
				return;
			} catch (Throwable t) {
				logger.warn("failed to rollback binlog message from server {}.\n{}", currentPumaClient.getPumaServerHost(),
				      ExceptionUtils.getStackTrace(t));

				restart(database, tables);
			}
		}
	}

	@Override
	public void subscribe(String database, List<String> tables, boolean dml, boolean ddl, boolean transaction) {
		this.database = database;
		this.tables = tables;
		this.dml = dml;
		this.ddl = ddl;
		this.transaction = transaction;

		if (currentPumaClient == null) {
			start(database, tables);
		}

		currentPumaClient.subscribe(database, tables, dml, ddl, transaction);
		subscribed = true;
	}

	@Override
	public void unSubscribe() throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to unsubscribe binlog, subscribe first.");
		}

		if (currentPumaClient == null) {
			start(database, tables);
		}

		currentPumaClient.unSubscribe();
		subscribed = false;
	}
}
