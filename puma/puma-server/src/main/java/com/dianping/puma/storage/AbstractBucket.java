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

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBucket implements Bucket {
	protected Sequence					startingSequence;
	protected int						maxSizeMB;
	protected AtomicReference<Sequence>	currentWritingSeq	= new AtomicReference<Sequence>();
	protected volatile boolean			stopped				= false;
	protected long						maxSizeByte;

	public AbstractBucket(Sequence startingSequence, int maxSizeMB) throws FileNotFoundException {
		this.startingSequence = startingSequence;
		this.maxSizeMB = maxSizeMB;
		this.maxSizeByte = this.maxSizeMB * 1024 * 1024L;
		// we need to copy the whole instance
		this.currentWritingSeq.set(new Sequence(startingSequence.getCreationDate(), startingSequence.getNumber()));
	}

	@Override
	public void append(byte[] data) throws StorageClosedException, IOException {
		checkClosed();
		doAppend(data);
		currentWritingSeq.set(currentWritingSeq.get().addOffset(data.length));
	}

	protected abstract void doAppend(byte[] data) throws IOException;

	@Override
	public byte[] getNext() throws StorageClosedException, IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (readable()) {
			return doReadData();
		} else {
			throw new EOFException();
		}
	}

	@Override
	public void seek(int pos) throws StorageClosedException, IOException {
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
	public boolean hasRemainingForWrite() throws StorageClosedException, IOException {
		checkClosed();
		return doHasRemainingForWrite();
	}

	protected abstract boolean doHasRemainingForWrite() throws IOException;

	protected abstract void doClose() throws IOException;

	protected abstract void doSeek(int pos) throws IOException;

	protected abstract boolean readable() throws IOException;

	protected abstract byte[] doReadData() throws StorageClosedException, IOException;

	protected void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Bucket has been closed");
		}
	}
}
