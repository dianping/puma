package com.dianping.puma.core.model;

public class BinlogInfo {

	private String binlogFile;

	private Long binlogPosition;

	private Boolean skipToNextPos = false;

	public BinlogInfo() {
	}

	public BinlogInfo(String binlogFile, Long binlogPosition) {
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
	}

	public String getBinlogFile() {
		return binlogFile;
	}

	public void setBinlogFile(String binlogFile) {
		this.binlogFile = binlogFile;
	}

	public Long getBinlogPosition() {
		return binlogPosition;
	}

	public void setBinlogPosition(Long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public Boolean isSkipToNextPos() {
		return skipToNextPos;
	}

	public void setSkipToNextPos(Boolean skipToNextPos) {
		this.skipToNextPos = skipToNextPos;
	}

	@Override
	public String toString() {
		return "BinlogInfo [binlogFile=" + binlogFile + ", binlogPosition=" + binlogPosition + ",skipToNextPos="
				+ skipToNextPos + " ]";
	}

}