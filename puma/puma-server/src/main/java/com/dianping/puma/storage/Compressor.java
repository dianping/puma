package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.dianping.puma.core.event.ChangedEvent;

public interface Compressor {
	
	public void compress(RandomAccessFile localFileAcess, DataOutput destFile, OutputStream destIndex) throws IOException;

	public DataInputStream getZipFileInputStream();
	
	public void readIn(byte[] data) throws IOException;
	
	public ChangedEvent getEvent(byte[] data) throws IOException;
	
	public void readByte() throws IOException;
	
	public byte[] uncompress() throws IOException;
}
