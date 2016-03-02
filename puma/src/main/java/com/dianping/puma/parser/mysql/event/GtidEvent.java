package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class GtidEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = -5675251301678869654L;
	private boolean commitFlag;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		commitFlag = (PacketUtils.readInt(buf, 1) != 0);
	}

	public void setCommitFlag(boolean commitFlag) {
		this.commitFlag = commitFlag;
	}

	public boolean isCommitFlag() {
		return commitFlag;
	}

	@Override
	public String toString() {
		return "GtidEvent [commitFlag = " + commitFlag + " , super.toString()= " + super.toString() + "]";
	}
}
