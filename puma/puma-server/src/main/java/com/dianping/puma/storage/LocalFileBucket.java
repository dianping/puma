package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 基于本地文件的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileBucket extends AbstractBucket {

    private RandomAccessFile file;

    public LocalFileBucket(File file, Sequence startingSequence, int maxSizeMB, String fileName) throws FileNotFoundException {
        super(startingSequence, maxSizeMB);
        this.fileName = fileName;
        this.file = new RandomAccessFile(file, "rw");
    }

    protected void doAppend(byte[] data) throws IOException {
        file.write(data);
    }

    protected byte[] doReadData() throws StorageClosedException, IOException {
        int length = file.readInt();
        byte[] data = new byte[length];
        int n = 0;
        while (n < length) {
            checkClosed();
            int count = file.read(data, 0 + n, length - n);
            n += count;
        }
        return data;
    }

    protected boolean readable() throws IOException {
        return file.getFilePointer() + 4 < file.length();
    }

    protected void doSeek(int pos) throws IOException {
        file.seek(pos);
    }

    protected void doClose() throws IOException {
        file.close();
        file = null;
    }

    protected boolean doHasRemainingForWrite() throws IOException {
        return file.length() < getMaxSizeByte();
    }

}
