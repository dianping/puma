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
package com.dianping.puma.storage.bucket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDataBucket implements DataBucket {
	private Sequence startingSequence;

	private int maxSizeMB;

	private AtomicReference<Sequence> currentWritingSeq = new AtomicReference<Sequence>();

	private volatile boolean stopped = false;

	private long maxSizeByte;

	protected long length;

	private String fileName;

	private boolean compress;

	protected DataInputStream input;

	protected DataOutputStream output;

	public String getBucketFileName() {
		return this.fileName;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the compress
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * @param maxSizeMB
	 *           the maxSizeMB to set
	 */
	public void setMaxSizeMB(int maxSizeMB) {
		this.maxSizeMB = maxSizeMB;
	}

	/**
	 * @param maxSizeByte
	 *           the maxSizeByte to set
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

	public AbstractDataBucket(Sequence startingSequence, int maxSizeMB, String fileName, boolean compress)
	      throws FileNotFoundException {
		this.startingSequence = startingSequence;
		this.maxSizeMB = maxSizeMB;
		this.maxSizeByte = this.maxSizeMB * 1024 * 1024L;
		this.fileName = fileName;
		this.compress = compress;
		// we need to copy the whole instance
		this.currentWritingSeq.set(new Sequence(startingSequence.getCreationDate(), startingSequence.getNumber()));
	}

	@Override
	public void append(byte[] data) throws StorageClosedException, IOException {
		checkClosed();
		doAppend(data);
		currentWritingSeq.set(currentWritingSeq.get().addOffset(data.length));
	}

	@Override
	public void flush() throws StorageClosedException, IOException {
		checkClosed();

		if (this.output != null) {
			this.output.flush();
		}
	}

	protected abstract void doAppend(byte[] data) throws IOException;

	@Override
	public byte[] getNext() throws StorageClosedException, IOException {
		checkClosed();
		input.mark(Integer.MAX_VALUE);

		try {
			int len = input.readInt();
			byte[] data = new byte[len];
			input.read(data);

			return data;
		} catch (EOFException eof) {
			input.reset();
			throw eof;
		}
	}

	@Override
	public void skip(int pos) throws StorageClosedException, IOException {
		checkClosed();
		doSkip(pos);
	}

	@Override
	public void stop() throws IOException {
		stopped = true;
		input.close();
		input = null;
		doClose();
	}

	@Override
	public Sequence getStartingSequece() {
		return startingSequence;
	}

	@Override
	public Sequence getCurrentWritingSeq() {
		return currentWritingSeq.get();
	}

	@Override
	public void start() throws IOException {
		stopped = false;
	}

	@Override
	public boolean hasRemainingForWrite() throws StorageClosedException, IOException {
		checkClosed();
		return doHasRemainingForWrite();
	}

	protected void doSkip(int pos) throws IOException {
		if (pos < 0) {
			throw new IOException(String.format("Seek %d pos failed(%s).", pos, getFileName()));
		}

		this.input.mark(Integer.MAX_VALUE);
		try {
			int count = pos;
			while (count > 0) {
				count -= input.skipBytes(count);
			}
		} catch (EOFException eof) {
			this.input.reset();

			throw eof;
		}
	}

	protected abstract boolean doHasRemainingForWrite() throws IOException;

	protected abstract void doClose() throws IOException;

	protected void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Bucket has been closed");
		}
	}
}
