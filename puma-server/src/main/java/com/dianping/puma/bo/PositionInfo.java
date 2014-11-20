package com.dianping.puma.bo;

public class PositionInfo {
	private long	binlogPosition;
	private String	binlogFileName;

	public PositionInfo(long binlogPosition, String binlogFileName) {
		this.binlogPosition = binlogPosition;
		this.binlogFileName = binlogFileName;
	}

	public long getBinlogPosition() {
		return binlogPosition;
	}

	public void setBinlogPosition(long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public String getBinlogFileName() {
		return binlogFileName;
	}

	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	@Override
	public String toString() {
		return "PositionInfor [binlogFileName=" + binlogFileName + ", binlogPosition=" + binlogPosition + "]";
	}

}
