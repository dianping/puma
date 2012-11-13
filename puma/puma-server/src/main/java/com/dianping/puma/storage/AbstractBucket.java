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
	private Sequence startingSequence;
	private BinlogInfoAndSeq startingBinlogInfoAndSeq;
	private int maxSizeMB;
	private AtomicReference<Sequence> currentWritingSeq = new AtomicReference<Sequence>();
	private AtomicReference<BinlogInfoAndSeq> currentWritingBinlogInfoAndSeq = new AtomicReference<BinlogInfoAndSeq>();
	private volatile boolean stopped = false;
	private long maxSizeByte;
	private Boolean isCompress = false;
	protected Compress compress;

	public BinlogInfoAndSeq getStartingBinlogInfoAndSeq() {
		return startingBinlogInfoAndSeq;
	}

	public void setStartingBinlogInfoAndSeq(BinlogInfoAndSeq startingBinlogInfoAndSeq) {
		this.startingBinlogInfoAndSeq = startingBinlogInfoAndSeq;
	}

	public BinlogInfoAndSeq getCurrentWritingBinlogInfoAndSeq() {
		return currentWritingBinlogInfoAndSeq.get();
	}

	public void setCurrentWritingBinlogInfoAndSeq(BinlogInfoAndSeq binlogInfoAndSeq) {
		BinlogInfoAndSeq temp = this.currentWritingBinlogInfoAndSeq.get();
		temp.setServerId(binlogInfoAndSeq.getServerId());
		temp.setBinlogFile(binlogInfoAndSeq.getBinlogFile());
		temp.setBinlogPosition(binlogInfoAndSeq.getBinlogPosition());
		this.currentWritingBinlogInfoAndSeq.set(temp);
	}

	public Boolean getIsCompress() {
		return isCompress;
	}

	public void setIsCompress(Boolean isCompress) {
		this.isCompress = isCompress;
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

	public AbstractBucket(Sequence startingSequence, int maxSizeMB) throws FileNotFoundException {
		this.startingSequence = startingSequence;
		this.maxSizeMB = maxSizeMB;
		this.maxSizeByte = this.maxSizeMB * 1024 * 1024L;
		// we need to copy the whole instance
		this.currentWritingSeq.set(new Sequence(startingSequence.getCreationDate(), startingSequence.getNumber()));
	}

	// TODO add getNext

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
		doSeek((int) offset);
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
	public boolean hasRemainingForWrite() throws StorageClosedException, IOException {
		checkClosed();
		return doHasRemainingForWrite();
	}

	@Override
	public byte[] getNext() throws StorageClosedException, IOException {
		checkClosed();
		// we should guarantee the whole packet read in one transaction,
		// otherwise we will skip some bytes and read a wrong value in the next
		// call
		if (!this.isCompress) {
			if (readable()) {
				byte[] data = doReadData();
				return data;
			} else {
				throw new EOFException();
			}
		} else {
			if (this.compress.getZipFileInputStream() == null) {
				if (readable()) {
					byte[] data = doReadData();
					// TODO 1. performance; 2. duplicated code; 3. ZIPFORMAT
					// only
					// appears in the first block, while we seek....; 4. file
					// format
					// desc
					this.compress.readIn(data);
					return getNextFromZipBuf();
				}else{
					throw new EOFException();
				}
			} else {
				return getNextFromZipBuf();
			}
		}
	}

	public byte[] getNextFromZipBuf() throws IOException {
		// TODO panduan zhe ge zip shi fou ke du, hai yao pan duan you mu you
		// xiayige zip
		try {
			return this.compress.unCompressNext();
		} catch (EOFException e) {
			if(readable()){
				byte[] data = doReadData();
				this.compress.readIn(data);
				return this.compress.unCompressNext();
			}else{
				throw new EOFException();
			}
		}
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
