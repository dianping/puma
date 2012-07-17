/**
 * Project: puma-server
 * 
 * File Created at 2012-7-17
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBucket implements Bucket {
	protected EventCodec				codec;
	protected Sequence					startingSequence;
	protected int						maxSizeMB;
	protected AtomicReference<Sequence>	currentWritingSeq	= new AtomicReference<Sequence>();
	protected volatile boolean			stopped				= false;

	public AbstractBucket(Sequence startingSequence, int maxSizeMB, EventCodec codec) throws FileNotFoundException {
		this.startingSequence = startingSequence;
		this.maxSizeMB = maxSizeMB;
		this.codec = codec;
		// we need to copy the whole instance
		this.currentWritingSeq.set(new Sequence(startingSequence.getCreationDate(), startingSequence.getNumber()));
	}

	@Override
	public void append(ChangedEvent event) throws IOException {
		checkClosed();
		byte[] data = codec.encode(event);
		doAppend(data);
		currentWritingSeq.set(currentWritingSeq.get().addOffset(4 + data.length));
	}

	protected abstract void doAppend(byte[] data) throws IOException;

	@Override
	public ChangedEvent getNext() throws IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (readable()) {
			byte[] data = doReadData();
			return codec.decode(data);
		} else {
			throw new EOFException();
		}
	}

	@Override
	public void seek(int pos) throws IOException {
		checkClosed();
		doSeek(pos);
	}

	@Override
	public void close() throws IOException {
		stopped = true;
		doClose();
	}

	@Override
	public Sequence getStartingSequece() {
		return startingSequence;
	}

	@Override
	public long getCurrentWritingSeq() {
		return currentWritingSeq.get().longValue();
	}

	@Override
	public boolean hasRemainingForWrite() throws IOException {
		checkClosed();
		return doHasRemainingForWrite();
	}

	protected abstract boolean doHasRemainingForWrite() throws IOException;

	protected abstract void doClose() throws IOException;

	protected abstract void doSeek(int pos) throws IOException;

	protected abstract boolean readable() throws IOException;

	protected abstract byte[] doReadData() throws IOException;

	protected void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket has been closed");
		}
	}
}
