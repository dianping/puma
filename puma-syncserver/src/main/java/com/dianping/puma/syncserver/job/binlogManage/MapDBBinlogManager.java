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

	/**
	 * MapDB binlog manager is stopped or not, default true.
	 */
	private boolean stopped = true;

	/**
	 * MapDB binlog manager exception, default null.
	 */
	private BinlogManageException binlogManageException = null;

	/**
	 * MapDB.
	 */
	private DB db;

	/**
	 * Unfinished binlog infos.
	 */
	private ConcurrentNavigableMap<BinlogInfo, Boolean> unfinished;

	/**
	 * Finished binlog infos.
	 */
	private ConcurrentNavigableMap<BinlogInfo, Boolean> finished;

	/**
	 * MapDB binlog manager title.
	 */
	private String title = "BinlogManager-";

	/**
	 * MapDB binlog manager name.
	 */
	private String name;

	/**
	 * The original binlog info, only for newly created task.
	 */
	private BinlogInfo origin;

	public MapDBBinlogManager(BinlogInfo origin) {
		this.origin = origin;
	}

	@Override
	public void start() {
		LOG.info("Starting binlog manager({})...", title + name);

		if (!stopped) {
			LOG.warn("Binlog manager({}) is already started.", title + name);
		} else {

			try {
				stopped = false;

				db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlogDB/", title + name)).closeOnJvmShutdown()
						.transactionDisable().asyncWriteEnable().mmapFileEnableIfSupported().make();

				// Read from the persistent storage.
				unfinished = db.getTreeMap(title + name + "-unfinished");
				finished = db.getTreeMap(title + name + "-finished");

				// Put the origin into the finished container if empty.
				if (finished.isEmpty()) {
					finished.put(origin, true);
					db.commit();
				}
			} catch (Exception e) {
				binlogManageException = BinlogManageException.translate(e);
				throw binlogManageException;
			} finally {
				stopped = false;
			}
		}
	}

	@Override
	public void stop() {
		LOG.info("Stopping binlog manager({})...", title + name);

		if (stopped) {
			LOG.info("Binlog manager({}) is already stopped.", title + name);
		} else {
			try {
				stopped = true;

				// Commit before close.
				db.commit();

				// Close db.
				db.close();
			} catch (Exception e) {
				binlogManageException = BinlogManageException.translate(e);
				throw binlogManageException;
			} finally {
				stopped = true;
			}
		}
	}

	@Override
	public void removeRecovery() {
		try {
			if (stopped) {
				db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlogDB/", title + name)).closeOnJvmShutdown()
						.transactionDisable().asyncWriteEnable().mmapFileEnableIfSupported().make();
			}

			// Delete persistent storage.
			db.delete(title + name + "-unfinished");
			db.delete(title + name + "-finished");
			db.commit();

			// Close db.
			db.close();
		} catch (Exception e) {
			binlogManageException = BinlogManageException.translate(e);
			throw binlogManageException;
		}
	}

	@ThreadSafe
	@Override
	public void before(BinlogInfo binlogInfo) {
		try {
			unfinished.put(binlogInfo, true);
		} catch (Exception e) {
			binlogManageException = BinlogManageException.translate(e);
			throw binlogManageException;
		}
	}

	@ThreadSafe
	@Override
	public void after(BinlogInfo binlogInfo) {
		try {
			finished.put(binlogInfo, true);
			finished.pollFirstEntry();
			unfinished.remove(binlogInfo);
		} catch (Exception e) {
			binlogManageException = BinlogManageException.translate(e);
			throw binlogManageException;
		}
	}

	@Override
	public BinlogInfo getRecovery() {
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
