package com.dianping.puma.storage;

import com.dianping.puma.core.event.ChangedEvent;

public class BinlogInfoAndSeq implements Comparable<BinlogInfoAndSeq>{

	private long serverId;
	// TODO
	private String binlogFile;
	// TODO
	private long binlogPosition;

	private long seq;

	private static final String BINLOGINFO_SEPARATOR = "$";

	public BinlogInfoAndSeq(long serverId, String binlogFile, long binlogPosition, long seq) {
		super();
		this.serverId = serverId;
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
		this.seq = seq;
	}
	
	public static BinlogInfoAndSeq getBinlogInfoAndSeq(ChangedEvent event) {
		return new BinlogInfoAndSeq(event.getServerId(), event.getBinlog(), event.getBinlogPos(), -1);
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

	public static BinlogInfoAndSeq valueOf(String key) {
		int begin = 0;
		int end = 0;
		long serverId;
		String binlogFile;
		long binlogPos;
		long seq;
		end = key.indexOf(BINLOGINFO_SEPARATOR);
		if (end == -1)
			return null;
		serverId = Long.valueOf(key.substring(begin, end)).longValue();
		begin = end + 1;
		end = key.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1)
			return null;
		binlogFile = key.substring(begin, end);
		begin = end + 1;
		end = key.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1)
			return null;
		binlogPos = Long.valueOf(key.substring(begin, end));
		begin = end + 1;
		end = key.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1) {
			return null;
		} else {
			seq = Long.valueOf(key.substring(begin, end)).longValue();
		}
		return new BinlogInfoAndSeq(serverId, binlogFile, binlogPos, seq);
	}
	
	public void setBinlogInfo(ChangedEvent event){
		this.serverId = event.getServerId();
		this.binlogFile = event.getBinlog();
		this.binlogPosition = event.getBinlogPos();
	}

	public Boolean binlogInfoEqual(BinlogInfoAndSeq value){
		if(this.serverId == value.getServerId() && this.binlogFile.equals(value) && this.binlogPosition == value.getBinlogPosition()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public int compareTo(BinlogInfoAndSeq o) {
		if (this.serverId < o.getServerId()) {
            return -1;
        } else if (this.serverId == o.getServerId()) {
            if (this.binlogFile.compareTo(o.getBinlogFile()) < 0) {
                return -1;
            } else if (this.binlogFile.compareTo(o.getBinlogFile()) == 0) {
                if (this.binlogPosition < o.getBinlogPosition()) {
                    return -1;
                } else if (this.binlogPosition == o.getBinlogPosition()) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        } else {
            return 1;
        }
	}
}
