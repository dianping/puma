package com.dianping.puma.server.Position;

public class PositionInfor {
	protected long binlogPosition;
	protected String binlogFileName = new String();

	public PositionInfor(long binlogPosition, String binlogFileName) {
		this.binlogPosition = binlogPosition;
		this.binlogFileName = binlogFileName;
	}

	public PositionInfor() {
		
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
		return "PositionInfor [binlogFileName=" + binlogFileName
				+ ", binlogPosition=" + binlogPosition + "]";
	}

}
