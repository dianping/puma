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
package com.dianping.puma.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DefaultDataIndexImpl<K extends DataIndexKey<K>, V> implements DataIndex<K, V> {
	static final String L2INDEX_FOLDER = "l2Index";

	static final String L2INDEX_FILESUFFIX = ".l2idx";

	static final String L1INDEX_FILENAME = "l1Index.l1idx";

	private String baseDir;

	private TreeMap<K, String> l1Index;

	private BufferedWriter l1IndexWriter;

	private String writingl2IndexName = null;

	private DataOutputStream writingl2IndexStream;

	private IndexItemConvertor<V> l2IndexItemConvertor;

	private IndexItemConvertor<K> keyConvertor;

	private ReentrantReadWriteLock l1Lock = new ReentrantReadWriteLock();

	private ReadLock l1ReadLock = l1Lock.readLock();

	private WriteLock l1WriteLock = l1Lock.writeLock();

	private ReentrantReadWriteLock l2Lock = new ReentrantReadWriteLock();

	private ReadLock l2ReadLock = l2Lock.readLock();

	private WriteLock l2WriteLock = l2Lock.writeLock();

	public DefaultDataIndexImpl(String baseDir, IndexItemConvertor<V> valueConvertor, IndexItemConvertor<K> keyConvertor) {
		this.baseDir = baseDir;
		this.keyConvertor = keyConvertor;
		this.l2IndexItemConvertor = valueConvertor;
	}

	@Override
	public V find(K key) {
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

			if (l2Index != null && l2Index.getValue() != null) {
				return findInL2Index(key, l2Index.getValue());
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	private V findInL2Index(K key, String l2IndexName) {
		l2ReadLock.lock();
		try {
			File l2IndexFile = getL2IndexFile(l2IndexName);
			if (!l2IndexFile.exists()) {
				return null;
			}

			InputStream is = null;
			try {
				LocalFileIndexBucket<K, V> indexBucket = new LocalFileIndexBucket<K, V>(l2IndexFile, l2IndexItemConvertor);
				indexBucket.start();

				return indexBucket.find(key);
			} catch (Exception e) {
				return null;
			} finally {
				closeQuietly(is);
			}
		} finally {
			l2ReadLock.unlock();
		}
	}

	private File getL2IndexFile(String l2IndexName) {
		return new File(new File(baseDir, L2INDEX_FOLDER), l2IndexName + L2INDEX_FILESUFFIX);
	}

	private File getL1IndexFile() {
		return new File(baseDir, L1INDEX_FILENAME);
	}

	@Override
	public void start() throws IOException {
		createDirIfNeeded();

		loadL1Index();

		l2WriteLock.lock();
		try {
			writingl2IndexName = null;
			closeQuietly(writingl2IndexStream);
			writingl2IndexStream = null;
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
					K key = keyConvertor.convertFromObj(propName);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.common.LifeCycle#stop()
	 */
	@Override
	public void stop() throws IOException {
		closeQuietly(l1IndexWriter);
		l1IndexWriter = null;
		closeQuietly(writingl2IndexStream);
		writingl2IndexName = null;
		writingl2IndexStream = null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.DataIndex#addL1Index(com.dianping.puma.storage .DataIndexKey, java.lang.String)
	 */
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
				closeQuietly(writingl2IndexStream);
				File l2IndexFile = getL2IndexFile(writingl2IndexName);
				l2IndexFile.createNewFile();
				writingl2IndexStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(l2IndexFile)));
			}
		} finally {
			l2WriteLock.unlock();
		}
	}

	private void appendL1IndexToFile(K key, String l2IndexName) throws IOException {
		l1IndexWriter.write(keyConvertor.convertToObj(key) + "=" + l2IndexName);
		l1IndexWriter.newLine();
		l1IndexWriter.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.DataIndex#addL2Index(com.dianping.puma.storage .DataIndexKey, java.lang.Object)
	 */
	@Override
	public void addL2Index(K key, V value) throws IOException {
		l2WriteLock.lock();
		try {
			if (writingl2IndexStream != null) {
				Object object = l2IndexItemConvertor.convertToObj(value);

				if (object instanceof byte[]) {
					byte[] bytes = (byte[]) object;

					writingl2IndexStream.writeByte(bytes.length);
					writingl2IndexStream.write(bytes);
				}

				writingl2IndexStream.flush();
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
					closeQuietly(writingl2IndexStream);
					writingl2IndexStream = null;
				}
				File l2IndexFile = getL2IndexFile(l2IndexName);
				FileUtils.deleteQuietly(l2IndexFile);
			} finally {
				l2WriteLock.unlock();
			}
		}
	}

	@Override
	public IndexBucket<K, V> getIndexBucket(K key) throws IOException {
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

			File l2IndexFile = getL2IndexFile(l2Index.getValue());

			return new LocalFileIndexBucket<K, V>(l2IndexFile, this.l2IndexItemConvertor);
		} else {
			return null;
		}
	}
}
