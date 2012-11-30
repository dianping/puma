package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public interface Compressor {
	
	public void compress(RandomAccessFile localFileAcess, DataOutput destFile, OutputStream destIndex) throws IOException;
	
	public void setInputStream(DataInputStream inputStream);
	
	public byte[] getNextEvent() throws IOException;
}
