package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;

public class ZipCompressor implements Compressor {
	private DataInputStream zipFileInputStream = null;
	private long zipThreshold = 512 * 1024;
	private EventCodec codec;
	private ArrayList<ZipIndexItem> zipIndex = new ArrayList<ZipIndexItem>();
	private static final String ZIPFORMAT = "ZIPFORMAT           ";
	private static final String ZIPINDEX_SEPARATOR = "$";
	private DataInputStream inputStream = null;
	public ArrayList<Long> binlogpos = new ArrayList<Long>();

	public long getZipThreshold() {
		return zipThreshold;
	}

	public void setZipThreshold(long zipThreshold) {
		this.zipThreshold = zipThreshold;
	}

	public DataInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(DataInputStream inputStream) {
		this.inputStream = inputStream;
	}

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

	private void readIn() throws IOException {
		this.zipFileInputStream = new DataInputStream(new GZIPInputStream(inputStream));
	}

	private byte[] uncompress() throws IOException {
		int eventSize = this.zipFileInputStream.readInt();
		byte[] unzipdata = new byte[eventSize];
		int n = 0;
		while (n < eventSize) {
			int len = this.zipFileInputStream.read(unzipdata, 0, eventSize - n);
			if (len == -1) {
				return null;
			}
			n += len;
		}
		return unzipdata;
	}

	public void compress(RandomAccessFile localFileAcess, DataOutput destFile, OutputStream destIndex) throws IOException {
		long offset = 0;
		destFile.write(ByteArrayUtils.intToByteArray(ZIPFORMAT.length()));
		destFile.write(ZIPFORMAT.getBytes());
		offset = offset + 4 + ZIPFORMAT.length();
		while (localFileAcess.getFilePointer() + 4 < localFileAcess.length()) {
			byte[] data = compressBlock(localFileAcess, offset);
			// destFile.write(ByteArrayUtils.intToByteArray(data.length));
			destFile.write(data);
			offset = offset + data.length;
		}
		if (zipIndex.isEmpty())
			return;
		writeZipIndex(destIndex);
		zipIndex.clear();
	}

	private byte[] compressBlock(RandomAccessFile localFileAcess, long offset) throws IOException {
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
			binlogpos.add(event.getBinlogPos());
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

	private void writeZipIndex(OutputStream ios) throws IOException {
		Properties properties = new Properties();
		for (int i = 0; i < zipIndex.size(); i++) {
			properties.put(String.valueOf(zipIndex.get(i).getBeginseq()) + ZIPINDEX_SEPARATOR
					+ String.valueOf(zipIndex.get(i).getEndseq()), String.valueOf(zipIndex.get(i).getOffset()));
		}
		properties.store(ios, "store zipIndex");
	}

	public byte[] getNextEvent() throws IOException {
		if (getZipFileInputStream() == null) {
			readIn();
		}
		return uncompress();
	}
}
