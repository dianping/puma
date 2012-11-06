/**
 * Project: puma-server
 * 
 * File Created at 2012-7-18
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public abstract class AbstractBucketIndex implements BucketIndex {
	protected static final String PATH_SEPARATOR = "/";
	protected AtomicReference<TreeMap<Sequence, String>> index = new AtomicReference<TreeMap<Sequence, String>>();
	protected String baseDir;
	private String bucketFilePrefix = "b-";
	protected int maxBucketLengthMB = 2000;
	private volatile boolean stopped = true;
	protected AtomicReference<Sequence> latestSequence = new AtomicReference<Sequence>();
	protected long zipThreshold = 200 * 1024 * 1024;
	protected String zipIndexsuffix = "-zipIndex";
	protected EventCodec codec;
	// TODO remove zipIndex, refactor to local
	protected ArrayList<ZipIndexItem> zipIndex = new ArrayList<ZipIndexItem>();

	public EventCodec getCodec() {
		return codec;
	}

	@Override
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	/**
	 * @return the index
	 */
	@Override
	public AtomicReference<TreeMap<Sequence, String>> getIndex() {
		return index;
	}

	/**
	 * @return the bucketFilePrefix
	 */
	public String getBucketFilePrefix() {
		return bucketFilePrefix;
	}

	/**
	 * @return the maxBucketLengthMB
	 */
	public int getMaxBucketLengthMB() {
		return maxBucketLengthMB;
	}

	/**
	 * @return the stop
	 */
	public boolean isStop() {
		return stopped;
	}

	/**
	 * @return the latestSequence
	 */
	public AtomicReference<Sequence> getLatestSequence() {
		return latestSequence;
	}

	public void setBucketFilePrefix(String bucketFilePrefix) {
		this.bucketFilePrefix = bucketFilePrefix;
	}

	public void setMaxBucketLengthMB(int maxBucketLengthMB) {
		this.maxBucketLengthMB = maxBucketLengthMB;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public void start() throws IOException {
		stopped = false;
	}

	@Override
	public void add(Bucket bucket) throws StorageClosedException {
		checkClosed();
		TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(
				index.get());
		newIndex.put(new Sequence(bucket.getStartingSequece()),
				convertToPath(bucket.getStartingSequece()));
		index.set(newIndex);
	}

	@Override
	public Bucket getNextReadBucket(Sequence sequence) throws IOException,
			StorageClosedException {
		checkClosed();
		NavigableMap<Sequence, String> tailMap = index.get().tailMap(sequence,
				false);
		if (!tailMap.isEmpty()) {
			Entry<Sequence, String> firstEntry = tailMap.firstEntry();
			return doGetReadBucket(baseDir, firstEntry.getValue(), firstEntry
					.getKey(), maxBucketLengthMB);
		}
		return null;
	}

	protected abstract Bucket doGetReadBucket(String baseDir, String path,
			Sequence startingSeq, int maxSizeMB) throws IOException;

	@Override
	public Bucket getNextWriteBucket() throws IOException,
			StorageClosedException {
		checkClosed();
		Entry<Sequence, String> lastEntry = index.get().lastEntry();
		Sequence nextSeq = null;
		if (lastEntry == null) {
			nextSeq = new Sequence(getNowCreationDate(), 0);
		} else {
			nextSeq = getNextWriteBucketSequence(new Sequence(lastEntry
					.getKey()));
		}
		String bucketPath = convertToPath(nextSeq);
		return doGetNextWriteBucket(baseDir, bucketPath, nextSeq);

	}

	protected abstract Bucket doGetNextWriteBucket(String baseDir,
			String bucketPath, Sequence startingSequence) throws IOException;

	protected int getNowCreationDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		return Integer.valueOf(sdf.format(new Date()));
	}

	protected Sequence getNextWriteBucketSequence(Sequence seq) {
		if (getNowCreationDate() == seq.getCreationDate()) {
			return seq.getNext(false);
		} else {
			return seq.getNext(true);
		}
	}

	protected String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR + bucketFilePrefix
				+ seq.getNumber();
	}

	protected int getDateFromPath(String path) {
		return Integer.valueOf(path.split(PATH_SEPARATOR)[0]);
	}

	protected Sequence convertToSequence(String path) {
		String[] parts = path.split(PATH_SEPARATOR);
		return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer
				.valueOf(parts[1].substring(bucketFilePrefix.length())));
	}

	public void stop() {
		if (stopped) {
			return;
		}
		stopped = true;
	}

	protected void checkClosed() throws StorageClosedException {
		if (stopped) {
			throw new StorageClosedException("Bucket index has been closed.");
		}
	}

	public boolean hasNexReadBucket(Sequence sequence)
			throws StorageClosedException {
		checkClosed();
		NavigableMap<Sequence, String> tailMap = index.get().tailMap(sequence,
				false);

		return !tailMap.isEmpty();
	}

	public int size() {
		return index.get().size();
	}

	public void add(List<String> paths) throws StorageClosedException {
		checkClosed();
		TreeMap<Sequence, String> newIndexes = new TreeMap<Sequence, String>(
				index.get());

		for (String path : paths) {
			newIndexes.put(convertToSequence(path), path);
		}

		index.set(newIndexes);
	}

	public List<String> bulkGetRemainN(int remainSize)
			throws StorageClosedException {
		checkClosed();
		List<String> results = new ArrayList<String>();
		TreeMap<Sequence, String> bakIndexes = index.get();

		int i = 0;
		for (Entry<Sequence, String> entry : bakIndexes.entrySet()) {
			if (i < index.get().size() - remainSize) {
				results.add(entry.getValue());
			} else {
				break;
			}
			i++;
		}
		return results;
	}

	@Override
	public List<String> bulkGetRemainNDay(int remainDay)
			throws StorageClosedException {
		checkClosed();
		List<String> results = new ArrayList<String>();
		TreeMap<Sequence, String> bakIndexes = index.get();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1 * (remainDay - 1));
		int preservedFromDay = Integer.valueOf(sdf.format(cal.getTime()));

		for (Entry<Sequence, String> entry : bakIndexes.entrySet()) {
			String path = entry.getValue();
			int date = getDateFromPath(path);
			if (date < preservedFromDay) {
				results.add(path);
			}
		}
		return results;
	}

	@Override
	public void remove(List<String> paths) throws StorageClosedException {
		checkClosed();
		TreeMap<Sequence, String> newIndexes = new TreeMap<Sequence, String>(
				index.get());

		for (String path : paths) {
			newIndexes.remove(convertToSequence(path));
		}

		index.set(newIndexes);
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void copyFromLocal(String srcBaseDir, String path)
			throws IOException, StorageClosedException {
		checkClosed();
	}

	@Override
	public boolean removeBucket(String path) throws StorageClosedException {
		checkClosed();
		return true;
	}

	@Override
	public void updateLatestSequence(Sequence sequence) {
		this.latestSequence.set(sequence);
	}

	protected static class PathSequenceComparator implements
			Comparator<Sequence>, Serializable {

		private static final long serialVersionUID = -350477869152651536L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Sequence o1, Sequence o2) {
			if (o1.getCreationDate() > o2.getCreationDate()) {
				return 1;
			} else if (o1.getCreationDate() < o2.getCreationDate()) {
				return -1;
			} else {
				if (o1.getNumber() > o2.getNumber()) {
					return 1;
				} else if (o1.getNumber() < o2.getNumber()) {
					return -1;
				} else {
					return 0;
				}
			}
		}

	}

	public byte[] readAndZip(RandomAccessFile localFileAcess, File file,
			long offset) throws IOException {
	    //TODO change num to readed
		long num = 0;
		long beginseq = 0;
		long endseq = 0;
		// TODO no need
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (num < this.zipThreshold) {
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
			bout.write(length);
			bout.write(data);
			num = num + length + 4;
			if (localFileAcess.getFilePointer() + 4 > localFileAcess.length())
				break;
		}
		this.zipIndex.add(new ZipIndexItem(beginseq, endseq, offset));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(bos);
		zip.write(bout.toByteArray());
		zip.close();
		bout.close();
		return bos.toByteArray();
	}

}
