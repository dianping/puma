package com.dianping.puma.storage;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.dianping.puma.core.event.ChangedEvent;

public class BinlogInfoAndSeq implements Comparable<BinlogInfoAndSeq> {

	private long serverId;
	private String binlogFile;
	private long binlogPosition;
	private long seq;

	private static final String BINLOGINFO_SEPARATOR = "$";
	private static final String BINLOGINFO_SEPARATOR_PATTERN = "\\$";

	public BinlogInfoAndSeq() {
		super();
	}

	public BinlogInfoAndSeq(long serverId, String binlogFile, long binlogPosition, long seq) {
		super();
		this.serverId = serverId;
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
		this.seq = seq;
	}

	public static BinlogInfoAndSeq getBinlogInfoAndSeq(ChangedEvent event) {
		return new BinlogInfoAndSeq(event.getServerId(), event.getBinlog(), event.getBinlogPos(), event.getSeq());
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public String getBinlogFile() {
		return this.binlogFile;
	}

	public void setBinlogFile(String binlogFile) {
		this.binlogFile = binlogFile;
	}

	public long getBinlogPosition() {
		return this.binlogPosition;
	}

	public void setBinlogPosition(long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public String toString() {
		return String.valueOf(this.serverId) + BINLOGINFO_SEPARATOR + this.binlogFile + BINLOGINFO_SEPARATOR
				+ String.valueOf(this.binlogPosition) + BINLOGINFO_SEPARATOR + String.valueOf(this.seq) + BINLOGINFO_SEPARATOR;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binlogFile == null) ? 0 : binlogFile.hashCode());
		result = prime * result + (int) (binlogPosition ^ (binlogPosition >>> 32));
		result = prime * result + (int) (seq ^ (seq >>> 32));
		result = prime * result + (int) (serverId ^ (serverId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinlogInfoAndSeq other = (BinlogInfoAndSeq) obj;
		if (binlogFile == null) {
			if (other.binlogFile != null)
				return false;
		} else if (!binlogFile.equals(other.binlogFile))
			return false;
		if (binlogPosition != other.binlogPosition)
			return false;
		if (seq != other.seq)
			return false;
		if (serverId != other.serverId)
			return false;
		return true;
	}

	public static BinlogInfoAndSeq valueOf(String key) {
		BinlogInfoAndSeq res = new BinlogInfoAndSeq();

		String[] splits = key.split(BINLOGINFO_SEPARATOR_PATTERN);

		if (splits != null && splits.length == 4) {
			res.setServerId(Long.valueOf(splits[0]));
			res.setBinlogFile(splits[1]);
			res.setBinlogPosition(Long.valueOf(splits[2]));
			res.setSeq(Long.valueOf(splits[3]));
			return res;
		} else {
			return null;
		}

	}

	public void setBinlogInfo(ChangedEvent event) {
		this.serverId = event.getServerId();
		this.binlogFile = event.getBinlog();
		this.binlogPosition = event.getBinlogPos();
	}

	public boolean binlogInfoEqual(BinlogInfoAndSeq value) {
		if (this.serverId == value.getServerId() && this.binlogFile.equals(value)
				&& this.binlogPosition == value.getBinlogPosition()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(BinlogInfoAndSeq o) {
		return CompareToBuilder.reflectionCompare(this, o, new String[] { "seq" });
	}

}
