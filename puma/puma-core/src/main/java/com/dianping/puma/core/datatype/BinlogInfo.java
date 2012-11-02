package com.dianping.puma.core.datatype;

public class BinlogInfo {
	private long serverId;
	private String BinlogFile;
	private long BinlogPosition;

	public BinlogInfo(long serverId, String binlogFile, long binlogPosition) {
		super();
		this.serverId = serverId;
		BinlogFile = binlogFile;
		BinlogPosition = binlogPosition;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public String getBinlogFile() {
		return BinlogFile;
	}

	public void setBinlogFile(String binlogFile) {
		BinlogFile = binlogFile;
	}

	public long getBinlogPosition() {
		return BinlogPosition;
	}

	public void setBinlogPosition(long binlogPosition) {
		BinlogPosition = binlogPosition;
	}

}
