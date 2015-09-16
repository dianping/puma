package com.dianping.puma.parser.mysql.event;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.PumaContext;

public class PreviousGtidsEvent extends AbstractBinlogEvent{

	private static final long serialVersionUID = 8809327351013249858L;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		return "PreviousGtidsEvent [super.toString()= " + super.toString() + "]";
	}
}
