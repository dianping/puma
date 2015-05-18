package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileBinlogManager implements BinlogManager {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileBinlogManager.class);

	private String name;

	private boolean stopped = true;
	private BinlogManageException binlogManageException;

	private BinlogInfo earliest;

	public void start() {
		stopped = false;
		binlogManageException = null;
	}

	public void stop() {
		stopped = true;
	}

	public BinlogManageException exception() {
		return binlogManageException;
	}

	public synchronized void save(BinlogInfo binlogInfo) {
		if (stopped) {
			LOG.info("BinlogManager is stopped for binlogInfo({}).", binlogInfo.toString());
			throw new BinlogManageException(0, String.format("BinlogManager is stopped for binlogInfo(%s).", binlogInfo.toString()));
		}

		if (binlogInfo.compareTo(earliest) < 0) {
			earliest = binlogInfo;
		}
	}

	public BinlogInfo getEarliest() {
		if (stopped) {
			LOG.info("BinlogManager is stopped.");
			throw new BinlogManageException(0, String.format("BinlogManager is stopped."));
		}

		return earliest;
	}

	public void setName(String name) {
		this.name = name;
	}
}
