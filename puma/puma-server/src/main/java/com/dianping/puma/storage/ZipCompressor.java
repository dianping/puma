package com.dianping.puma.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;

public class ZipCompressor implements Compressor {
	private DataInputStream zipFileInputStream = null;
	private long zipThreshold = 200 * 1024 * 1024;
	private EventCodec codec;
	private ByteArrayOutputStream haveReadData = null;
	private int eventSize;
	private int nowoff;

	public DataInputStream getZipFileInputStream() {
		return zipFileInputStream;
	}

	public void setZipFileInputStream(DataInputStream zipFileInputStream) {
		this.zipFileInputStream = zipFileInputStream;
	}

	public EventCodec getCodec() {
		return codec;
	}

	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void readIn(byte[] data) throws IOException {
		if (this.zipFileInputStream != null) {
			this.zipFileInputStream.close();
		}
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		this.zipFileInputStream = new DataInputStream(new GZIPInputStream(bin));
	}

	public void readByte() throws IOException {
		this.eventSize = this.zipFileInputStream.readInt();
		this.nowoff = 0;
		this.haveReadData = new ByteArrayOutputStream();
	}

	public byte[] uncompress() throws IOException {
		while (true) {
			byte[] unzipdata = new byte[512];
			int len = this.zipFileInputStream.read(unzipdata, 0, this.eventSize - this.nowoff);
			if (len == -1) {
				return null;
			}
			this.haveReadData.write(new String(unzipdata).substring(0, len).getBytes());
			this.nowoff = this.nowoff + len;
			if (this.nowoff == this.eventSize) {
				byte[] result = this.haveReadData.toByteArray();
				this.haveReadData.close();
				return result;
			}
		}
	}

	public byte[] compress(RandomAccessFile localFileAcess, long offset, ArrayList<ZipIndexItem> zipIndex) throws IOException {
		// TODO change num to readed
		long readed = 0;
		long beginseq = 0;
		long endseq = 0;
		// TODO no need
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (readed < this.zipThreshold) {
			int length = localFileAcess.readInt();
			byte[] data = new byte[length];
			int n = 0;
			while (n < length) {
				int count = localFileAcess.read(data, 0 + n, length - n);
				n += count;
			}
			ChangedEvent event = (ChangedEvent) codec.decode(data);
			if (beginseq == 0)
				beginseq = event.getSeq();
			endseq = event.getSeq();
			bout.write(ByteArrayUtils.intToByteArray(length));
			bout.write(data);
			readed = readed + length + 4;
			if (localFileAcess.getFilePointer() + 4 > localFileAcess.length())
				break;
		}
		zipIndex.add(new ZipIndexItem(beginseq, endseq, offset));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bos);
		zip.write(bout.toByteArray());
		zip.close();
		bout.close();
		return bos.toByteArray();
	}

	public ChangedEvent getEvent(byte[] data) throws IOException {
		return (ChangedEvent) this.codec.decode(data);
	}
}
