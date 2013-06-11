package com.dianping.puma.storage;

import java.io.IOException;

public interface DecoderableQueue {

	public void put(DecoderElement e) throws InterruptedException;

	public DecoderElement take(long timeout) throws IOException,
			InterruptedException;

	public void close();

	int size();

}
