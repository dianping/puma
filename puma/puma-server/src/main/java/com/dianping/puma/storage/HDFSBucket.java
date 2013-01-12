package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * 基于HDFS的Bucket实现
 * 
 * @author Leo Liang
 * 
 */
public class HDFSBucket extends AbstractBucket {

    private Path file;

    public HDFSBucket(FileSystem fileSystem, String baseDir, String path, Sequence startingSequence, boolean compress)
            throws IOException {
        super(startingSequence, -1, path, compress);
        this.file = new Path(baseDir, path);
        if (!compress) {
            this.input = new DataInputStream(fileSystem.open(file));
        } else {
            this.input = new DataInputStream(new GZIPInputStream(fileSystem.open(file)));
        }
    }

    protected void doAppend(byte[] data) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void doClose() throws IOException {
    }

    @Override
    public long getCurrentWritingSeq() {
        throw new UnsupportedOperationException();
    }

    protected boolean doHasRemainingForWrite() throws IOException {
        throw new UnsupportedOperationException();
    }

}