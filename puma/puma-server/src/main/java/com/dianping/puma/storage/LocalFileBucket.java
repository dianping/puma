package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

/**
 * 基于本地文件的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileBucket extends AbstractBucket {

    private RandomAccessFile file;

    public LocalFileBucket(File file, Sequence startingSequence, int maxSizeMB, String fileName, boolean compress)
            throws IOException {
        super(startingSequence, maxSizeMB, fileName, compress);
        this.file = new RandomAccessFile(file, "rw");
        if (!compress) {
            input = new DataInputStream(new FileInputStream(file));
        } else {
            input = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
        }
    }

    protected void doAppend(byte[] data) throws IOException {
        file.write(data);
    }

    protected void doClose() throws IOException {
        file.close();
        file = null;
    }

    protected boolean doHasRemainingForWrite() throws IOException {
        return file.length() < getMaxSizeByte();
    }

}
