package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public class IgnorableEvent extends AbstractBinlogEvent {
	
	private static final long serialVersionUID = 4410827697732528116L;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String toString(){
		return "IgnorableEvent [ super.toString()= " + super.toString() + " ]";
	}
}
