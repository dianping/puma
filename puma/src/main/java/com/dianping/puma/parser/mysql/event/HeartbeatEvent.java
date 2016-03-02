package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HeartbeatEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = -4085386552164788243L;
	private String logIdentify;
	
	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		int lenRemaining = lenRemaining(buf, context);
		logIdentify = PacketUtils.readFixedLengthString(buf, lenRemaining);
	}
	public void setLogIdentify(String logIdentify) {
		this.logIdentify = logIdentify;
	}
	public String getLogIdentify() {
		return logIdentify;
	}

	@Override
	public String toString(){
		return "HeartbeatEvent [ logIdentify= "+this.logIdentify+" , super.toString()= " + super.toString() + " ]";
	}
}
