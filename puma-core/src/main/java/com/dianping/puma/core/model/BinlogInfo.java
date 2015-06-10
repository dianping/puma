package com.dianping.puma.core.model;

import java.io.Serializable;

public class BinlogInfo implements Serializable, Comparable<BinlogInfo> {

	private static final long serialVersionUID = 5056491879587690001L;

	private String binlogFile;

	private Long binlogPosition;

	private int eventIndex;

	public BinlogInfo() {
	}

	public BinlogInfo(String binlogFile, Long binlogPosition) {
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
		this.eventIndex = 0;
	}

	public BinlogInfo(String binlogFile, Long binlogPosition, int eventIndex) {
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
		this.eventIndex = eventIndex;
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

	public int getEventIndex() {
		return eventIndex;
	}

	public void setEventIndex(int eventIndex) {
		this.eventIndex = eventIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof BinlogInfo)) {
			return false;
		} else {
			BinlogInfo binlogInfo = (BinlogInfo) o;
			if (this.binlogFile.equals(binlogInfo.getBinlogFile())
					&& this.getBinlogPosition().longValue() == binlogInfo.getBinlogPosition().longValue()
					&& this.getEventIndex() == binlogInfo.getEventIndex()) {
				return true;
			}
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = this.getBinlogFile().hashCode();
		result = 31 * result + (int) (this.getBinlogPosition() ^ (this.getBinlogPosition() >>> 32));
		result = 31 * result + this.eventIndex;
		return result;
	}

	@Override
	public String toString() {
		return "BinlogInfo [binlogFile=" + binlogFile + ", binlogPosition=" + binlogPosition + ", eventIndex="
				+ eventIndex + " ]";
	}

	public int compareTo(BinlogInfo binlogInfo) {
		String leftBinlogFile = this.getBinlogFile();
		int leftBinlogFileNum = Integer.parseInt(leftBinlogFile.substring(leftBinlogFile.indexOf(".") + 1));
		String rightBinlogFile = binlogInfo.getBinlogFile();
		int rightBinlogFileNum = Integer.parseInt(rightBinlogFile.substring(rightBinlogFile.indexOf(".") + 1));

		if (leftBinlogFileNum < rightBinlogFileNum) {
			return -1;
		} else if (leftBinlogFileNum > rightBinlogFileNum) {
			return 1;
		} else {
			long leftBinlogPosition = this.getBinlogPosition();
			long rightBinlogPosition = binlogInfo.getBinlogPosition();

			if (leftBinlogPosition < rightBinlogPosition) {
				return -1;
			} else if (leftBinlogPosition > rightBinlogPosition) {
				return 1;
			} else {
				if (this.getEventIndex() == binlogInfo.getEventIndex()) {
					return 0;
				}else if(this.getEventIndex() > binlogInfo.getEventIndex()){
					return 1;
				}else{
					return -1;
				}
			}
		}
	}

}