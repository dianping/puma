package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientFactory;
import com.dianping.puma.api.PumaServerRouter;
import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClusterPumaClient implements PumaClient {

	private final Logger logger = LoggerFactory.getLogger(ClusterPumaClient.class);

	private final String name;

	/**
	 * Puma client subscription options.
	 */
	private String database;
	private List<String> tables;
	private boolean dml = true; // default only need dml.
	private boolean ddl = false;
	private boolean transaction = false;

	/**
	 * Puma client ha options.
	 */
	private final int retryTimes = 3; // puma client ha retry times.
	private final int retryInterval = 3000;
	private PumaServerRouter pumaServerRouter;

	/**
	 * Current puma client state.
	 */
	private volatile String currentHost;
	private volatile boolean subscribed = false;
	private volatile PumaClient currentPumaClient;

	public ClusterPumaClient(String name) {
		this.name = name;
		start();
	}

	protected void start() {
		try {
			currentHost = pumaServerRouter.next();
			currentPumaClient = PumaClientFactory.createSimplePumaClient(name, currentHost);
		} catch (Throwable t) {
			throw new PumaClientException("fail to start puma client from server " + currentHost + ".", t);
		}
	}

	protected void restart() {
		try {
			currentHost = pumaServerRouter.next();
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

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.get(batchSize);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message from server({}).\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}

		throw new PumaClientException("failed to get binlog message after " + retryTimes + " times retries.");
	}

	@Override
	public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.get(batchSize, timeout, timeUnit);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message from server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}

		throw new PumaClientException("failed to get binlog message after " + retryTimes + " time retries.");
	}

	@Override
	public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message with ack, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.getWithAck(batchSize, timeout, timeUnit);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message with ack from server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}

		throw new PumaClientException("failed to get binlog message with ack after " + retryTimes + " time retries.");
	}

	@Override
	public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to get binlog message with ack, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.getWithAck(batchSize);
			} catch (Throwable t) {
				logger.warn("failed to get binlog message with ack from server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}

		throw new PumaClientException("failed to get binlog message with ack after " + retryTimes + " time retries.");
	}

	@Override
	public void ack(BinlogInfo binlogInfo) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to ack binlog position, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.ack(binlogInfo);
			} catch (Throwable t) {
				logger.warn("failed to ack binlog position to server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}
	}

	@Override
	public void rollback() throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to rollback binlog message, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.rollback();
			} catch (Throwable t) {
				logger.warn("failed to rollback binlog message from server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
			}
		}
	}

	@Override
	public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to rollback binlog message, subscribe first.");
		}

		for (int i = 0; i != retryTimes; ++i) {
			try {
				currentPumaClient.rollback(binlogInfo);
			} catch (Throwable t) {
				logger.warn("failed to rollback binlog message from server {}.\n{}",
						currentPumaClient.getPumaServerHost(),
						ExceptionUtils.getStackTrace(t));

				restart();
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

		currentPumaClient.subscribe(database, tables, dml, ddl, transaction);

		subscribed = true;
	}

	@Override
	public void unSubscribe() throws PumaClientException {
		if (!subscribed) {
			throw new PumaClientException("failed to unsubscribe binlog, subscribe first.");
		}

		currentPumaClient.unSubscribe();
		subscribed = false;
	}

	public void setPumaServerRouter(PumaServerRouter pumaServerRouter) {
		this.pumaServerRouter = pumaServerRouter;
	}
}
