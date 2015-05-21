package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentNavigableMap;

public class MapDBBinlogManager implements BinlogManager {

	private static final Logger LOG = LoggerFactory.getLogger(MapDBBinlogManager.class);

	private String name;

	private boolean stopped = true;
	private BinlogManageException binlogManageException;

	private DB db;

	private ConcurrentNavigableMap<BinlogInfo, Boolean> unfinished;

	private ConcurrentNavigableMap<BinlogInfo, Boolean> finished;

	private BinlogInfo origin;

	public MapDBBinlogManager() {}

	public MapDBBinlogManager(BinlogInfo origin) {
		this.origin = origin;
	}

	public void start() {
		LOG.info("Starting binlog manager({})...", name);

		if (!stopped) {
			LOG.warn("Binlog manager({}) is already started.", name);
		} else {
			stopped = false;
			binlogManageException = null;

			db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlog/", name)).closeOnJvmShutdown().transactionDisable()
					.asyncWriteEnable().mmapFileEnableIfSupported().make();

			// Read from the persistent storage.
			unfinished = db.getTreeMap(name + "-unfinished");
			finished = db.getTreeMap(name + "-finished");

			// Put into the original binlog position.
			if (finished.isEmpty()) {
				finished.put(origin, true);
			}
		}
	}

	public void stop() {
		LOG.info("Stopping binlog manager({})...", name);

		if (stopped) {
			LOG.info("Binlog manager({}) is already stopped.", name);
		} else {
			stopped = true;

			// Release db resource.
			db.commit();
			db.close();
		}
	}

	public void destroy() {
		LOG.info("Destroying binlog manager({})...", name);

		stopped = true;

		// Delete persistent storage.
		db.delete(name + "-unfinished");
		db.delete(name + "-finished");

		// Release db resource.
		db.commit();
		db.close();
	}

	public BinlogManageException exception() {
		return binlogManageException;
	}

	@ThreadSafe
	public void before(BinlogInfo binlogInfo) {
		if (stopped) {
			LOG.info("BinlogManager({}) is already stopped for binlogInfo({}).", name);
			throw new BinlogManageException(0, String.format("BinlogManager(%s) is stopped for binlogInfo.", name));
		}

		unfinished.put(binlogInfo, true);
	}

	@ThreadSafe
	public void after(BinlogInfo binlogInfo) {
		if (stopped) {
			LOG.info("BinlogManager({}) is already stopped for binlogInfo.", name);
			throw new BinlogManageException(0, String.format("BinlogManager(%s) is stopped for binlogInfo.", name));
		}

		finished.put(binlogInfo, true);
		finished.pollFirstEntry();
		unfinished.remove(binlogInfo);
	}

	public BinlogInfo getRecovery() {
		if (stopped) {
			LOG.info("BinlogManager({}) is already stopped for binlogInfo.", name);
			throw new BinlogManageException(0, String.format("BinlogManager(%s) is stopped for binlogInfo.", name));
		}

		try {
			return unfinished.firstKey();
		} catch (NoSuchElementException e) {
			try {
				return finished.lastKey();
			} catch (NoSuchElementException e1) {
				return origin;
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}
}
