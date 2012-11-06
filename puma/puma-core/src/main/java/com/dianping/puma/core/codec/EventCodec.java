package com.dianping.puma.core.codec;

import java.io.IOException;

public interface EventCodec {
	public static final int	DDL_EVENT	= 0;
	public static final int	DML_EVENT	= 1;

	public byte[] encode(Object object) throws IOException;

	public Object decode(byte[] data) throws IOException;

}
