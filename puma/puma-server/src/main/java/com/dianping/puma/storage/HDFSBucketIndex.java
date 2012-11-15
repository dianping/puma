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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;

import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * TODO Comment of HDFSBucketIndex
 * 
 * @author Leo Liang
 * 
 */
public class HDFSBucketIndex extends AbstractBucketIndex {

	private Configuration hdfsConfig;
	private FileSystem fileSystem;

	public void initHdfsConfiguration() {
		hdfsConfig = new Configuration();
		Properties prop = new Properties();
		InputStream propIn = null;

		try {
			propIn = DefaultBucketManager.class.getClassLoader().getResourceAsStream("hdfs.properties");
			prop.load(propIn);

			for (String key : prop.stringPropertyNames()) {
				hdfsConfig.set(key, prop.getProperty(key));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (propIn != null) {
				try {
					propIn.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		UserGroupInformation.setConfiguration(hdfsConfig);
		try {
			SecurityUtil.login(hdfsConfig, prop.getProperty("keytabFileKey"), prop.getProperty("userNameKey"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketIndex#init()
	 */
	@Override
	public void start() throws IOException {
		initHdfsConfiguration();
		this.fileSystem = FileSystem.get(this.hdfsConfig);

		TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>();
		getIndex().set(newIndex);

		if (this.fileSystem.getFileStatus(new Path(this.getBaseDir())).isDir()) {

			FileStatus[] dirsStatus = this.fileSystem.listStatus(new Path(this.getBaseDir()));
			if (dirsStatus != null && dirsStatus.length != 0) {

				Path[] listedPaths = FileUtil.stat2Paths(dirsStatus);

				for (Path pathname : listedPaths) {

					if (this.fileSystem.getFileStatus(pathname).isDir()) {
						if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {

							FileStatus[] status = this.fileSystem.listStatus(pathname);
							Path[] listedFiles = FileUtil.stat2Paths(status);

							for (Path subFile : listedFiles) {
								if (subFile.getName().startsWith(getBucketFilePrefix())
										&& StringUtils.isNumeric(subFile.getName().substring(getBucketFilePrefix().length()))) {
									String path = pathname.getName() + PATH_SEPARATOR + subFile.getName();
									newIndex.put(convertToSequence(path), path);
								}
							}
						}
					}

				}
			}
		}
		super.start();
	}

	@Override
	protected Bucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB) throws IOException {
		return new HDFSBucket(fileSystem, baseDir, path, startingSeq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.AbstractBucketIndex#close()
	 */
	@Override
	public void stop() {
		super.stop();
		try {
			this.fileSystem.close();
		} catch (IOException e) {
			// ignore
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.storage.AbstractBucketIndex#doGetNextWriteBucket(java
	 * .lang.String, java.lang.String, com.dianping.puma.storage.Sequence)
	 */
	@Override
	protected Bucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence) throws IOException {
		return null;
	}

	@Override
	public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
		super.copyFromLocal(srcBaseDir, path);
		File localFile = new File(srcBaseDir, path);
		if (!localFile.exists()) {
			return;
		}
		ArrayList<ZipIndexItem> zipIndex = new ArrayList<ZipIndexItem>();
		FSDataOutputStream fos = fileSystem.create(new Path(this.getBaseDir(), path));
		RandomAccessFile localFileAcess = new RandomAccessFile(localFile, "rw");
		fos.write(ZIPFORMAT.length());
		fos.write(ZIPFORMAT.getBytes());
		while (localFileAcess.getFilePointer() + 4 < localFileAcess.length()) {
			byte[] data = this.compress.compress(localFileAcess, fos.size(), zipIndex);
			fos.write(ByteArrayUtils.intToByteArray(data.length));
			fos.write(data);
		}
		FSDataOutputStream ios = fileSystem.create(new Path(this.getBaseDir(), path + this.zipIndexsuffix));
		if (zipIndex.isEmpty())
			return;
		writeZipIndex(zipIndex, ios);
		ios.close();
		fos.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.storage.AbstractBucketIndex#removeBucket(java.lang.
	 * String)
	 */
	@Override
	public boolean removeBucket(String path) throws StorageClosedException {
		super.removeBucket(path);

		boolean deleted = false;

		try {
			Path p = new Path(getBaseDir(), path);
			if (this.fileSystem.exists(p)) {
				deleted = this.fileSystem.delete(p, false);
				if (deleted)
					this.fileSystem.delete(new Path(getBaseDir(), path + this.zipIndexsuffix), false);
			}

			if (this.fileSystem.exists(p.getParent())) {
				FileStatus[] listStatus = this.fileSystem.listStatus(p.getParent());
				if (listStatus == null || listStatus.length == 0) {
					this.fileSystem.delete(p.getParent(), false);
				}
			}

			return deleted;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public ArrayList<ZipIndexItem> readZipIndex(String baseDir, String path) throws IOException {
		Properties properties = new Properties();
		Path file = new Path(baseDir, path);
		FSDataInputStream inputStream = this.fileSystem.open(file);
		properties.load(inputStream);
		inputStream.close();
		ArrayList<ZipIndexItem> results = new ArrayList<ZipIndexItem>();
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys) {
			ZipIndexItem item = new ZipIndexItem(Long.valueOf(key.substring(0, key.indexOf(ZIPINDEX_SEPARATOR))).longValue(), Long.valueOf(
					key.substring(key.indexOf(ZIPINDEX_SEPARATOR) + 1)).longValue(), Long.valueOf(properties.getProperty(key)));
			results.add(item);
		}
		return results;
	}
}
