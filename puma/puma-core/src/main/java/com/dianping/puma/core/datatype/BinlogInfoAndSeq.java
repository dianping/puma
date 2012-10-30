package com.dianping.puma.core.datatype;

public class BinlogInfoAndSeq {
	private BinlogInfo binloginfo;
	private long seq;
	public BinlogInfoAndSeq(long serverId, String binlogfile, long binloginfo, long seq){
		this.binloginfo = new BinlogInfo(serverId,binlogfile,binloginfo);
		this.seq = seq;
	}
	public BinlogInfoAndSeq(BinlogInfo binloginfo, long seq){
		this.binloginfo = binloginfo;
		this.seq = seq;
	}
	public BinlogInfo getBinlogInfo() {
		return binloginfo;
	}
	public void setBinlogInfo(BinlogInfo binloginfo) {
		this.binloginfo = binloginfo;
	}
	public long getSeq() {
		return seq;
	}
	public void setSeq(long seq) {
		this.seq = seq;
	}
}
