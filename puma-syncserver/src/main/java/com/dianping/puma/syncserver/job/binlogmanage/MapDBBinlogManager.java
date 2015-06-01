package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;

import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MapDBBinlogManager implements BinlogManager {

	private static final Logger LOG = LoggerFactory.getLogger(MapDBBinlogManager.class);

	private boolean inited = false;

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
	private ConcurrentNavigableMap<Long, BinlogInfo> unfinished;

	/**
	 * Finished binlog infos.
	 */
	private ConcurrentNavigableMap<Long, BinlogInfo> finished;

	/**
	 * MapDB binlog manager title.
	 */
	private String title = "BinlogManager-";

	/**
	 * MapDB binlog manager name.
	 */
	private String name;

	private final long minSeq = Long.MIN_VALUE;

	private long oriSeq;

	private BinlogInfo oriBinlogInfo;

	public MapDBBinlogManager(long oriSeq, BinlogInfo oriBinlogInfo) {
		this.oriSeq = oriSeq;
		this.oriBinlogInfo = oriBinlogInfo;
	}

	@Override
	public void init() {
		if (inited) {
			return;
		}

		db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlog/", title + name)).closeOnJvmShutdown()
				.transactionDisable().asyncWriteEnable().mmapFileEnableIfSupported().make();

		// Read from the persistent storage.
		unfinished = db.getTreeMap(title + name + "-unfinished");
		finished = db.getTreeMap(title + name + "-finished");

		inited = true;
	}

	/*
	@Override
	public void destroy() {
		if (!inited) {
			return;
		}

		db.commit();
		db.close();

		inited = false;
	}*/

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		stopped = false;
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;
	}

	@Override
	public void destroy() {
		if (db.isClosed()) {
			db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlog/", title + name)).closeOnJvmShutdown()
					.transactionDisable().asyncWriteEnable().mmapFileEnableIfSupported().make();
		}

		// Delete persistent storage.
		finished.clear();
		unfinished.clear();
		db.delete(title + name + "-unfinished");
		db.delete(title + name + "-finished");
		db.commit();

		// Close db.
		db.close();

		inited = false;
	}

	@ThreadSafe
	@Override
	public void before(long seq, BinlogInfo binlogInfo) {
		try {
			unfinished.put(seq, binlogInfo);
		} catch (Exception e) {
			binlogManageException = BinlogManageException.translate(e);
			throw binlogManageException;
		}
	}

	@ThreadSafe
	@Override
	public void after(long seq, BinlogInfo binlogInfo) {
		try {
			finished.put(seq, binlogInfo);

			if (finished.size() > 10) {
				finished.pollFirstEntry();
			}

			unfinished.remove(seq);
		} catch (Exception e) {
			binlogManageException = BinlogManageException.translate(e);
			throw binlogManageException;
		}
	}

	@Override
	public BinlogInfo getBinlogInfo() {
		if (unfinished.lastEntry() != null) {
			return unfinished.lastEntry().getValue();
		} else {
			if (finished.firstEntry() != null) {
				return finished.firstEntry().getValue();
			} else {
				return oriBinlogInfo;
			}
		}
	}

	@Override
	public long getSeq() {
		if (unfinished.lastEntry() != null) {
			return unfinished.lastEntry().getKey();
		} else {
			if (finished.firstEntry() != null) {
				return finished.firstEntry().getKey();
			} else {
				return oriSeq;
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}
}
