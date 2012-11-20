package com.dianping.puma.storage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;

public interface Compressor {

	public void setCodec(EventCodec codec);
	
	public byte[] compress(RandomAccessFile localFileAcess, long offset, ArrayList<ZipIndexItem> zipIndex) throws IOException;

	public DataInputStream getZipFileInputStream();
	
	public void readIn(byte[] data) throws IOException;
	
	public byte[] unCompressNext() throws IOException;
	
	public ChangedEvent getEvent(byte[] data) throws IOException;
}
