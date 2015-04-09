package com.dianping.puma.core.model.state.client;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.speed.SpeedStat;
import com.dianping.puma.core.model.state.State;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ClientState extends State {

	private String taskName;

	private String ip;

	// Current client sequence, not next.
	private long seq;

	private BinlogInfo binlogInfo;

	private SpeedStat seqSpeedStat;

	public ClientState() {
		init();
	}

	public ClientState(String name, String taskName, String ip) {
		super(name);
		this.taskName = taskName;
		this.ip = ip;

		init();
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

	public double getSeqSpeedPerSecond() {
		return seqSpeedStat.getSpeedPerSecond();
	}

	private void init() {
		seqSpeedStat = new SpeedStat();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				seqSpeedStat.record((double) seq, new Date());
			}
		}, 30*1000, 30*1000);
	}
}
