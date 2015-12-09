package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.utils.ZipUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

public final class LengthReadBucket extends AbstractLifeCycle implements ReadBucket {

    private static final int INTEGER_SIZE_BYTE = Integer.SIZE >> 3;

    private final File file;

    private final int bufSizeByte;

    private final int avgSizeByte;

    private DataInputStream input;

    private int position;

    protected LengthReadBucket(File file, int bufSizeByte, int avgSizeByte) {
        this.file = file;
        this.bufSizeByte = bufSizeByte;
        this.avgSizeByte = avgSizeByte;
    }

    @Override
    protected void doStart() {
        try {
            input = file2Stream(file);
            if (!input.markSupported()) {
                throw new UnsupportedOperationException("length read bucket should support mark.");
            }
        } catch (IOException io) {
            throw new IllegalStateException("failed to start read bucket.", io);
        }
    }

    @Override
    protected void doStop() {
        try {
            input.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public byte[] next() throws IOException {
        checkStop();

        try {
            input.mark(avgSizeByte);

            int len = input.readInt();
            if (len <= 0) {
                throw new IOException("failed to read next data.");
            }

            byte[] data = new byte[len];
            input.readFully(data);
            position += (data.length + INTEGER_SIZE_BYTE);
            return data;
        } catch (IOException io) {
            try {
                input.reset();
            } catch (IOException ignore) {
            }

            throw io;
        }
    }

    @Override
    public void skip(long offset) throws IOException {
        checkStop();

        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative");
        }

        long count = offset;
        while (count > 0) {
            long skipLength = input.skip(count);
            count -= skipLength;
        }
        position += offset;
    }

    @Override
    public int position() {
        return position;
    }

    protected DataInputStream file2Stream(File file) throws IOException {
        if (!file.canRead()) {
            throw new IOException("bucket can not read.");
        }

        if (checkCompressed(file)) {
            input = new DataInputStream(new BufferedInputStream(
                    new GZIPInputStream(new FileInputStream(file), bufSizeByte)));
        } else {
            input = new DataInputStream(new BufferedInputStream(new FileInputStream(file), bufSizeByte));
        }

        return input;
    }

    protected boolean checkCompressed(File file) throws FileNotFoundException {
        return ZipUtils.checkGZip(file);
    }
}
