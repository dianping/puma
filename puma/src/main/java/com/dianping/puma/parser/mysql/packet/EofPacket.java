package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EofPacket extends AbstractResponsePacket {

	private static final long serialVersionUID = 6041203932772303842L;

	private boolean ok;

	@Override
	protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
		// TODO Auto-generated method stub
		if (buf.get() != -2) {
			ok = false;
			return;
		}
		ok = true;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

}
