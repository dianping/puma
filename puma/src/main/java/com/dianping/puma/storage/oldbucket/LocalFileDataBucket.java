package com.dianping.puma.storage.oldbucket;

import com.dianping.puma.storage.Sequence;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * 基于本地文件的Bucket实现
 *
 * @author Leo Liang
 */
public class LocalFileDataBucket extends AbstractDataBucket {

    private static final int BUF_SIZE = 1024 * 100;

    public LocalFileDataBucket(File file, Sequence startingSequence, int maxSizeMB, String fileName, boolean compress)
            throws IOException {
        super(startingSequence, maxSizeMB, fileName, compress);
        this.length = file.length();
        this.output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true), BUF_SIZE));
        if (!compress) {
            input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), BUF_SIZE));
        } else {
            input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)), BUF_SIZE));
        }
    }

    protected void doAppend(byte[] data) throws IOException {
        this.length += data.length;
        this.output.write(data);
    }

    protected void doClose() throws IOException {
        this.output.close();
    }

    protected boolean doHasRemainingForWrite() throws IOException {
        return this.length < getMaxSizeByte();
    }
}
