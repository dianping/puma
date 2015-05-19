package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;

public class MapDBBinlogManager implements BinlogManager {

	private static final Logger LOG = LoggerFactory.getLogger(MapDBBinlogManager.class);

	private String name;

	private boolean stopped = true;
	private BinlogManageException binlogManageException;

	private DB db;

	private ConcurrentNavigableMap<BinlogInfo, Boolean> binlogInfos;

	public void start() {
		stopped = false;
		binlogManageException = null;

		db = DBMaker.newFileDB(new File("/data/appdatas/puma/binlog/", name)).closeOnJvmShutdown().transactionDisable()
				.asyncWriteEnable().mmapFileEnableIfSupported().make();
		binlogInfos = db.getTreeMap(name);
	}

	public void stop() {
		stopped = true;

		db.close();
	}

	public void delete() {
		db.delete(name);
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

		binlogInfos.put(binlogInfo, true);
	}

	@ThreadSafe
	public void after(BinlogInfo binlogInfo) {
		if (stopped) {
			LOG.info("BinlogManager({}) is already stopped for binlogInfo.", name);
			throw new BinlogManageException(0, String.format("BinlogManager(%s) is stopped for binlogInfo.", name));
		}

		binlogInfos.remove(binlogInfo);
	}

	public BinlogInfo getEarliest() {
		if (stopped) {
			LOG.info("BinlogManager({}) is already stopped for binlogInfo.", name);
			throw new BinlogManageException(0, String.format("BinlogManager(%s) is stopped for binlogInfo.", name));
		}

		return binlogInfos.firstKey();
	}

	public void setName(String name) {
		this.name = name;
	}
}
