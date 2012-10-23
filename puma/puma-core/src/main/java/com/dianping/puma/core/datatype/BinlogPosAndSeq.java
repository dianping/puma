package com.dianping.puma.core.datatype;

public class BinlogPosAndSeq {
	private BinlogPos binlogpos;
	private long seq;
	public BinlogPosAndSeq(long serverId, String binlogfile, long binlogpos, long seq){
		this.binlogpos = new BinlogPos(serverId,binlogfile,binlogpos);
		this.seq = seq;
	}
	public BinlogPosAndSeq(BinlogPos binlogpos, long seq){
		this.binlogpos = binlogpos;
		this.seq = seq;
	}
	public BinlogPos getBinlogpos() {
		return binlogpos;
	}
	public void setBinlogpos(BinlogPos binlogpos) {
		this.binlogpos = binlogpos;
	}
	public long getSeq() {
		return seq;
	}
	public void setSeq(long seq) {
		this.seq = seq;
	}
}
