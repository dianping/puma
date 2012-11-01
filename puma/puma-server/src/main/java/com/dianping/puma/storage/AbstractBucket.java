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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import com.dianping.puma.core.datatype.BinlogInfo;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBucket implements Bucket {
	private Sequence startingSequence;
	private BinlogInfo startingBinlogInfo;
	private int maxSizeMB;
	private AtomicReference<Sequence> currentWritingSeq = new AtomicReference<Sequence>();
	private AtomicReference<BinlogInfo> currentWritingBinlogInfo = new AtomicReference<BinlogInfo>();
	private volatile boolean stopped = false;
	private long maxSizeByte;

	public BinlogInfo getStartingBinlogInfo() {
		return startingBinlogInfo;
	}

	public void setStartingBinlogInfo(BinlogInfo startingBinlogInfo) {
		this.startingBinlogInfo = startingBinlogInfo;
	}

	public BinlogInfo getCurrentWritingBinlogInfo() {
		return currentWritingBinlogInfo.get();
	}

	public void setCurrentWritingBinlogInfo(BinlogInfo binlogInfo) {
		this.currentWritingBinlogInfo.set(binlogInfo);
	}

	/**
	 * @param maxSizeMB
	 *            the maxSizeMB to set
	 */
	public void setMaxSizeMB(int maxSizeMB) {
		this.maxSizeMB = maxSizeMB;
	}

	/**
	 * @param maxSizeByte
	 *            the maxSizeByte to set
	 */
	public void setMaxSizeByte(long maxSizeByte) {
		this.maxSizeByte = maxSizeByte;
	}

	/**
	 * @return the maxSizeMB
	 */
	public int getMaxSizeMB() {
		return maxSizeMB;
	}

	/**
	 * @return the stopped
	 */
	public boolean isStopped() {
		return stopped;
	}

	/**
	 * @return the maxSizeByte
	 */
	public long getMaxSizeByte() {
		return maxSizeByte;
	}

	public AbstractBucket(Sequence startingSequence, int maxSizeMB)
			throws FileNotFoundException {
		this.startingSequence = startingSequence;
		this.maxSizeMB = maxSizeMB;
		this.maxSizeByte = this.maxSizeMB * 1024 * 1024L;
		// we need to copy the whole instance
		this.currentWritingSeq.set(new Sequence(startingSequence
				.getCreationDate(), startingSequence.getNumber()));
	}

	@Override
	public void append(byte[] data) throws StorageClosedException, IOException {
		checkClosed();
		doAppend(data);
		currentWritingSeq.set(currentWritingSeq.get().addOffset(data.length));
	}

	protected abstract void doAppend(byte[] data) throws IOException;

	@Override
	public void seek(long offset) throws StorageClosedException, IOException {
		checkClosed();
		doSeek((int)offset);
	}

	@Override
	public void stop() throws IOException {
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
	public void start() throws IOException {

	}

	@Override
	public boolean hasRemainingForWrite() throws StorageClosedException,
			IOException {
		checkClosed();
		return doHasRemainingForWrite();
	}

	protected abstract boolean doHasRemainingForWrite() throws IOException;

	protected abstract void doClose() throws IOException;

	protected abstract void doSeek(int pos) throws IOException;

	protected abstract boolean readable() throws IOException;

	protected abstract byte[] doReadData() throws StorageClosedException,
			IOException;

	protected void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Bucket has been closed");
		}
	}
}
