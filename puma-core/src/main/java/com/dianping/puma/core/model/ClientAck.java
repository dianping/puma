package com.dianping.puma.core.model;

import java.util.Date;

public class ClientAck extends AbstractAck {

	private static final long serialVersionUID = -2922003739900121110L;

	private String clientName;

	private BinlogInfo binlogInfo;

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

}
