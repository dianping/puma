/**
 * Project: puma-server
 * 
 * File Created at 2013-1-8
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
package com.dianping.puma.storage.index;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 * @author damon.zhu
 * 
 */
public class DefaultIndexManager<K extends IndexKey<K>, V extends IndexValue<K>> implements IndexManager<K, V> {

	private static final int BUF_SIZE = 1024 * 10;

	public static final String L1INDEX_FILENAME = "l1Index.l1idx";

	public static final String L2INDEX_FOLDER = "l2Index";

	public static final String L2INDEX_FILESUFFIX = ".l2idx";

	private String baseDir;

	private TreeMap<K, String> l1Index;

	private BufferedWriter l1IndexWriter;

	private String writingl2IndexName = null;

	private DataOutputStream l2IndexWriter;

	private IndexItemConvertor<K> indexKeyConvertor;

	private IndexItemConvertor<V> indexValueConvertor;

	private ReentrantReadWriteLock l1Lock = new ReentrantReadWriteLock();

	private ReadLock l1ReadLock = l1Lock.readLock();

	private WriteLock l1WriteLock = l1Lock.writeLock();

	private ReentrantReadWriteLock l2Lock = new ReentrantReadWriteLock();

	private WriteLock l2WriteLock = l2Lock.writeLock();

	private AtomicReference<K> latestL2Index = new AtomicReference<K>();

	public DefaultIndexManager(String baseDir, IndexItemConvertor<K> indexKeyConvertor,
	      IndexItemConvertor<V> indexValueConvertor) {
		this.baseDir = baseDir;
		this.indexKeyConvertor = indexKeyConvertor;
		this.indexValueConvertor = indexValueConvertor;
	}

	private File getL2IndexFile(String l2IndexName) {
		return new File(new File(baseDir, L2INDEX_FOLDER), l2IndexName + L2INDEX_FILESUFFIX);
	}

	private File getL1IndexFile() {
		return new File(baseDir, L1INDEX_FILENAME);
	}

	/*
	 * for test purpose only
	 */
	protected void setLatestL2IndexNull() {
		latestL2Index.getAndSet(null);
	}

	@Override
	public void start() throws IOException {
		createDirIfNeeded();

		loadL1Index();

		l2WriteLock.lock();
		try {
			writingl2IndexName = null;
			closeQuietly(l2IndexWriter);
			l2IndexWriter = null;
		} finally {
			l2WriteLock.unlock();
		}
	}

	private void loadL1Index() throws IOException {
		l1WriteLock.lock();
		try {
			l1Index = new TreeMap<K, String>();
			File l1IndexFile = getL1IndexFile();
			l1IndexFile.createNewFile();

			InputStream is = null;
			try {
				Properties prop = new Properties();
				is = new FileInputStream(l1IndexFile);
				prop.load(is);
				TreeMap<K, String> newL1Index = new TreeMap<K, String>();
				for (String propName : prop.stringPropertyNames()) {
					K key = indexKeyConvertor.convertFromObj(propName);
					String value = prop.getProperty(propName);
					if (key != null && value != null) {
						newL1Index.put(key, value);
					}
				}
				l1Index = newL1Index;
				l1IndexWriter = new BufferedWriter(new FileWriter(l1IndexFile, true));
			} finally {
				closeQuietly(is);
			}
		} finally {
			l1WriteLock.unlock();
		}
	}

	private void createDirIfNeeded() throws IOException {
		File folder = new File(baseDir);
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new IOException(String.format("Create dir(%s) failed.", baseDir));
			}
		}

		File l2IndexFolder = new File(baseDir, L2INDEX_FOLDER);
		if (!l2IndexFolder.exists()) {
			if (!l2IndexFolder.mkdirs()) {
				throw new IOException(String.format("Create dir(%s) failed.", l2IndexFolder.getAbsolutePath()));
			}
		}
	}

	@Override
	public void flush() throws IOException {
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}
	}

	@Override
	public void stop() throws IOException {
		closeQuietly(l1IndexWriter);
		l1IndexWriter = null;
		closeQuietly(l2IndexWriter);
		writingl2IndexName = null;
		l2IndexWriter = null;
	}

	private void closeQuietly(Writer out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void closeQuietly(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	@Override
	public void addL1Index(K key, String l2IndexName) throws IOException {
		boolean added = false;
		l1WriteLock.lock();
		try {
			if (!l1Index.containsKey(key)) {
				l1Index.put(key, l2IndexName);
				appendL1IndexToFile(key, l2IndexName);
				added = true;
			}
		} finally {
			l1WriteLock.unlock();
		}

		l2WriteLock.lock();
		try {
			if (added && !StringUtils.equals(l2IndexName, writingl2IndexName)) {
				writingl2IndexName = l2IndexName;
				closeQuietly(l2IndexWriter);
				File l2IndexFile = getL2IndexFile(writingl2IndexName);
				l2IndexFile.createNewFile();
				l2IndexWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(l2IndexFile), BUF_SIZE));
			}
		} finally {
			l2WriteLock.unlock();
		}
	}

	private void appendL1IndexToFile(K key, String l2IndexName) throws IOException {
		l1IndexWriter.write(indexKeyConvertor.convertToObj(key) + "=" + l2IndexName);
		l1IndexWriter.newLine();
		l1IndexWriter.flush();
	}

	@Override
	public void addL2Index(K key, V value) throws IOException {
		l2WriteLock.lock();
		try {
			if (l2IndexWriter != null) {
				Object object = indexValueConvertor.convertToObj(value);

				if (object instanceof byte[]) {
					byte[] bytes = (byte[]) object;

					l2IndexWriter.writeInt(bytes.length);
					l2IndexWriter.write(bytes);
				}

				latestL2Index.set(key);
			}
		} finally {
			l2WriteLock.unlock();
		}
	}

	@Override
	public void removeByL2IndexName(String l2IndexName) throws IOException {
		boolean removed = false;
		l1WriteLock.lock();
		try {
			if (l1Index.containsValue(l2IndexName)) {
				K tobeRemoveKey = null;
				for (K key : l1Index.keySet()) {
					if (l2IndexName.equals(l1Index.get(key))) {
						tobeRemoveKey = key;
						break;
					}
				}

				if (tobeRemoveKey != null) {
					l1Index.remove(tobeRemoveKey);
					removed = true;
					closeQuietly(l1IndexWriter);
					l1IndexWriter = null;
					FileUtils.deleteQuietly(getL1IndexFile());
					l1IndexWriter = new BufferedWriter(new FileWriter(getL1IndexFile()));
					for (Map.Entry<K, String> entry : l1Index.entrySet()) {
						appendL1IndexToFile(entry.getKey(), entry.getValue());
					}
				}
			}
		} finally {
			l1WriteLock.unlock();
		}

		if (removed) {
			l2WriteLock.lock();
			try {
				if (StringUtils.equals(l2IndexName, writingl2IndexName)) {
					writingl2IndexName = null;
					closeQuietly(l2IndexWriter);
					l2IndexWriter = null;
				}
				File l2IndexFile = getL2IndexFile(l2IndexName);
				FileUtils.deleteQuietly(l2IndexFile);
			} finally {
				l2WriteLock.unlock();
			}
		}
	}

	@Override
	public IndexBucket<K, V> getIndexBucket(K key, boolean inclusive) throws IOException {
		if (key != null && l1Index != null) {
			Entry<K, String> l2Index = null;
			l1ReadLock.lock();
			try {
				if (l1Index.isEmpty()) {
					return null;
				}

				l2Index = l1Index.floorEntry(key);
			} finally {
				l1ReadLock.unlock();
			}

			if (l2Index != null) {
				File l2IndexFile = getL2IndexFile(l2Index.getValue());
				LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(l2IndexFile,
				      this.indexValueConvertor);

				localFileIndexBucket.locate(key, inclusive);

				return localFileIndexBucket;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean hasNextIndexBucket(K key) throws IOException {
		Entry<K, String> l2Index = null;
		if (l1Index != null) {
			l1ReadLock.lock();
			try {
				if (l1Index.isEmpty()) {
					return false;
				}

				l2Index = l1Index.tailMap(key, true).firstEntry();
			} finally {
				l1ReadLock.unlock();
			}
		}

		return l2Index != null ? true : false;
	}

	public IndexBucket<K, V> getNextIndexBucket(K key) throws IOException {
		Entry<K, String> l2Index = null;
		if (l1Index != null) {
			l1ReadLock.lock();
			try {
				if (l1Index.isEmpty()) {
					return null;
				}

				l2Index = l1Index.tailMap(key, true).firstEntry();
			} finally {
				l1ReadLock.unlock();
			}
		}

		if (l2Index != null) {
			File l2IndexFile = getL2IndexFile(l2Index.getValue());
			LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(l2IndexFile,
			      this.indexValueConvertor);

			return localFileIndexBucket;
		} else {
			return null;
		}
	}

	@Override
	public K findFirst() throws IOException {
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}

		if (l1Index != null) {
			l1ReadLock.lock();

			try {
				if (l1Index.isEmpty()) {
					return null;
				} else {
					return l1Index.firstKey();
				}
			} finally {
				l1ReadLock.unlock();
			}
		} else {
			return null;
		}
	}

	@Override
	public K findLatest() throws IOException {
		K latestKey = latestL2Index.get();
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}

		if (latestKey != null) {
			return latestKey;
		} else {
			if (l1Index != null) {
				l1ReadLock.lock();

				try {
					if (l1Index.isEmpty()) {
						return null;
					} else {
						Entry<K, String> lastEntry = l1Index.lastEntry();

						LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(
						      getL2IndexFile(lastEntry.getValue()), this.indexValueConvertor);
						bucket.start();

						V next = null;
						try {
							while (true) {
								next = bucket.next();
							}
						} catch (EOFException ignore) {
						} finally {
							bucket.stop();
						}

						if (next != null) {
							return next.getIndexKey();
						} else {
							return null;
						}
					}
				} finally {
					l1ReadLock.unlock();
				}
			} else {
				return null;
			}
		}
	}

	@Override
	public K findByTime(K searchKey, boolean startWithCompleteTransaction) throws IOException {
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}

		if (l1Index != null) {
			l1ReadLock.lock();

			try {
				if (l1Index.isEmpty()) {
					return null;
				} else {
					Entry<K, String> target = l1Index.floorEntry(searchKey);

					if (target == null) {
						target = l1Index.ceilingEntry(searchKey);

						if (target != null) {
							return target.getKey();
						} else {
							return null;
						}
					}

					LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(getL2IndexFile(target.getValue()),
					      this.indexValueConvertor);
					bucket.start();

					V next = null;
					LinkedList<V> entries = new LinkedList<V>();

					try {
						while (true) {
							next = bucket.next();

							if (next.getIndexKey().getTimestamp() > searchKey.getTimestamp()) {
								break;
							} else {
								entries.add(next);
							}
						}
					} catch (EOFException ignore) {
					} finally {
						bucket.stop();
					}

					if (startWithCompleteTransaction) {
						// 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
						V last = entries.pollLast();

						while (last != null) {
							if (last.isTransactionCommit()) {
								return last.getIndexKey();
							} else {
								last = entries.pollLast();
							}
						}
						// 如果仍然没有找到
						return findFromPreviosIndexBucketByTime(target.getKey());
					} else {
						V last = entries.pollLast();

						return (last == null) ? null : last.getIndexKey();
					}
				}
			} finally {
				l1ReadLock.unlock();
			}
		} else {
			return null;
		}
	}

	private K findFromPreviosIndexBucketByTime(K searchKey) throws IOException {
		Entry<K, String> target = l1Index.headMap(searchKey, false).lastEntry();

		if (target == null) {
			return null;
		}

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(getL2IndexFile(target.getValue()),
		      this.indexValueConvertor);
		bucket.start();

		K commitKey = null;

		try {
			while (true) {
				V next = bucket.next();

				if (next.isTransactionCommit()) {
					commitKey = next.getIndexKey();
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (commitKey != null) {
			return commitKey;
		} else {
			return findFromPreviosIndexBucketByTime(target.getKey());
		}
	}

	@Override
	public K findByBinlog(K searchKey, boolean startWithCompleteTransaction) throws IOException {
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}

		if (l1Index != null) {
			l1ReadLock.lock();

			try {
				if (l1Index.isEmpty()) {
					return null;
				} else {
					Entry<K, String> target = null;

					for (Entry<K, String> entry : l1Index.entrySet()) {
						K key = entry.getKey();

						if (key.getServerId() == searchKey.getServerId()) {
							if (compareTo(searchKey, key) >= 0) {

								if (target == null) {
									target = entry;
								} else {
									if (compareTo(key, target.getKey()) >= 0) {
										target = entry;
									}
								}
							}
						}
					}

					if (target == null) {
						return null;
					}

					LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(getL2IndexFile(target.getValue()),
					      this.indexValueConvertor);
					bucket.start();

					V next = null;
					LinkedList<V> entries = new LinkedList<V>();

					try {
						while (true) {
							next = bucket.next();

							if (compareTo(next.getIndexKey(), searchKey) > 0) {
								break;
							} else {
								entries.add(next);
							}
						}
					} catch (EOFException ignore) {
					} finally {
						bucket.stop();
					}

					if (startWithCompleteTransaction) {
						// 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
						V last = entries.pollLast();

						while (last != null) {
							if (last.isTransactionCommit()) {
								return last.getIndexKey();
							} else {
								last = entries.pollLast();
							}
						}

						// 如果仍然没有找到
						return findFromPreviosIndexBucketByBinlog(target.getKey());
					} else {
						V last = entries.pollLast();

						return (last == null) ? null : last.getIndexKey();
					}
				}
			} finally {
				l1ReadLock.unlock();
			}
		} else {
			return null;
		}
	}

	private K findFromPreviosIndexBucketByBinlog(K searchKey) throws IOException {
		Entry<K, String> target = null;

		for (Entry<K, String> entry : l1Index.entrySet()) {
			K key = entry.getKey();

			if (key.getServerId() == searchKey.getServerId()) {
				if (compareTo(searchKey, key) > 0) {

					if (target == null) {
						target = entry;
					} else {
						if (compareTo(key, target.getKey()) > 0) {
							target = entry;
						}
					}
				}
			}
		}

		if (target == null) {
			return null;
		}

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(getL2IndexFile(target.getValue()),
		      this.indexValueConvertor);
		bucket.start();

		K commitKey = null;

		try {
			while (true) {
				V next = bucket.next();

				if (next.isTransactionCommit()) {
					commitKey = next.getIndexKey();
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (commitKey != null) {
			return commitKey;
		} else {
			return findFromPreviosIndexBucketByBinlog(target.getKey());
		}
	}

	private int compareTo(K key1, K key2) {
		if (key1.getServerId() == key2.getServerId()) {
			if (key1.getBinlogFile().equals(key2.getBinlogFile())) {
				if (key1.getBinlogPosition() == key2.getBinlogPosition()) {
					return 0;
				} else {
					return key1.getBinlogPosition() > key2.getBinlogPosition() ? 1 : -1;
				}
			} else {
				return key1.getBinlogFile().compareTo(key2.getBinlogFile());
			}
		} else {
			return key1.getServerId() > key2.getServerId() ? 1 : -1;
		}
	}

	@Override
	public TreeMap<K, String> getL1Index() {
		return this.l1Index;
	}

	@Override
	public IndexBucket<K, V> getIndexBucket(String fileName) throws IOException {
		File l2IndexFile = getL2IndexFile(fileName);
		LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(l2IndexFile,
		      this.indexValueConvertor);

		return localFileIndexBucket;
	}
}
