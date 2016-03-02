package com.dianping.puma.parser.mysql.event;

import com.dianping.puma.common.PumaContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AnonymousGtidEvent extends AbstractBinlogEvent {

	private static final long serialVersionUID = -7261766467455896919L;

	@Override
	public void doParse(ByteBuffer buf, PumaContext context) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString(){
		return "AnonymousGtidEvent [ super.toString()= " + super.toString() + " ]";
	}
}
