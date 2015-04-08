package com.dianping.puma.core.model.state.client;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.state.State;

public class ClientState extends State {

	private String taskName;

	private String ip;

	// Current client sequence, not next.
	private long seq;

	private BinlogInfo binlogInfo;

	public ClientState(String name, String taskName, String ip) {
		super(name);
		this.taskName = taskName;
		this.ip = ip;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
