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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractDataBucket implements DataBucket {
    private Sequence                  startingSequence;
    private int                       maxSizeMB;
    private AtomicReference<Sequence> currentWritingSeq = new AtomicReference<Sequence>();
    private volatile boolean          stopped           = false;
    private long                      maxSizeByte;
    private String                    fileName;
    private boolean                   compress;
    protected DataInputStream         input;
    protected ByteBuffer              dataLengthBuf     = ByteBuffer.allocate(4);

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
    public long getCurrentWritingSeq() {
        return currentWritingSeq.get().longValue();
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

    protected boolean readable() throws IOException {
        while (dataLengthBuf.hasRemaining()) {
            int b = input.read();
            if (b != -1) {
                dataLengthBuf.put((byte) b);
            } else {
                break;
            }
        }
        return !dataLengthBuf.hasRemaining();
    }

    protected byte[] doReadData() throws StorageClosedException, IOException {
        dataLengthBuf.flip();
        int length = dataLengthBuf.getInt();
        dataLengthBuf.clear();
        ByteBuffer data = ByteBuffer.allocate(length);
        while (data.hasRemaining()) {
            checkClosed();
            int b = input.read();
            if (b != -1) {
                data.put((byte) b);
            }
        }
        return data.array();
    }

    protected void doSkip(int pos) throws IOException {
        if (pos < 0) {
            throw new IOException(String.format("Seek %d pos failed(%s).", pos, getFileName()));
        }

        int count = pos;
        while (count > 0) {
            count -= input.skipBytes(count);
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
