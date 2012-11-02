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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;

import com.dianping.puma.core.event.ChangedEvent;
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
			propIn = DefaultBucketManager.class.getClassLoader()
					.getResourceAsStream("hdfs.properties");
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
			SecurityUtil.login(hdfsConfig, prop.getProperty("keytabFileKey"),
					prop.getProperty("userNameKey"));
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

		TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(
				new PathSequenceComparator());
		getIndex().set(newIndex);

		if (this.fileSystem.getFileStatus(new Path(this.getBaseDir())).isDir()) {

			FileStatus[] dirsStatus = this.fileSystem.listStatus(new Path(this
					.getBaseDir()));
			if (dirsStatus != null && dirsStatus.length != 0) {

				Path[] listedPaths = FileUtil.stat2Paths(dirsStatus);

				for (Path pathname : listedPaths) {

					if (this.fileSystem.getFileStatus(pathname).isDir()) {
						if (StringUtils.isNumeric(pathname.getName())
								&& pathname.getName().length() == 8) {

							FileStatus[] status = this.fileSystem
									.listStatus(pathname);
							Path[] listedFiles = FileUtil.stat2Paths(status);

							for (Path subFile : listedFiles) {
								if (subFile.getName().startsWith(
										getBucketFilePrefix())
										&& StringUtils.isNumeric(subFile
												.getName().substring(
														getBucketFilePrefix()
																.length()))) {
									String path = pathname.getName()
											+ PATH_SEPARATOR
											+ subFile.getName();
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
	protected Bucket doGetReadBucket(String baseDir, String path,
			Sequence startingSeq, int maxSizeMB) throws IOException {
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
	protected Bucket doGetNextWriteBucket(String baseDir, String bucketPath,
			Sequence startingSequence) throws IOException {
		return null;
	}

	@Override
	public void copyFromLocal(String srcBaseDir, String path)
			throws IOException, StorageClosedException {
		super.copyFromLocal(srcBaseDir, path);
		File localFile = new File(srcBaseDir, path);
		if (!localFile.exists()) {
			return;
		}
		zipIndex.clear();
		FSDataOutputStream fos = fileSystem.create(new Path(this.getBaseDir(),
				path));
		RandomAccessFile localFileAcess = new RandomAccessFile(localFile, "rw");
		fos.write("ZIPFORMAT".length());
		fos.write("ZIPFORMAT".getBytes());
		while (localFileAcess.getFilePointer() + 4 < localFileAcess.length()) {
			byte[] data = readAndZip(localFileAcess, localFile, fos.size());
			fos.write(data.length);
			fos.write(data);
		}
		FSDataOutputStream ios = fileSystem.create(new Path(this.getBaseDir(),
				path + this.zipIndexsuffix));
		if(zipIndex.isEmpty())
			return;
		byte[] index = this.codec.encode(zipIndex);
		ios.write(index);
		ios.close();
		fos.close();

		// fileSystem.copyFromLocalFile(false, true, new Path(srcBaseDir, path),
		// new Path(this.getBaseDir(), path));
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
					this.fileSystem.delete(new Path(getBaseDir(), path
							+ this.zipIndexsuffix), false);
			}

			if (this.fileSystem.exists(p.getParent())) {
				FileStatus[] listStatus = this.fileSystem.listStatus(p
						.getParent());
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
	public Bucket getReadBucket(long seq, Boolean start)
			throws StorageClosedException, IOException {
		checkClosed();
		Sequence sequence = null;
		String path = null;

		if (seq == -1L) {
			// 从最老开始消费
			if (!index.get().isEmpty()) {
				path = index.get().firstEntry().getValue();
				if (path == null) {
					return null;
				}
				sequence = new Sequence(index.get().firstEntry().getKey());
			} else {
				return null;
			}
		} else if (seq == -2L) {
			// 从最新开始消费
			if (this.latestSequence.get() != null) {
				sequence = new Sequence(this.latestSequence.get());
				path = convertToPath(sequence);
			} else {
				return null;
			}
		} else {
			sequence = new Sequence(seq);
			path = index.get().get(sequence);
			if (path == null) {
				return null;
			}

		}

		int offset = sequence.getOffset();
		Bucket bucket = doGetReadBucket(baseDir, path, sequence.clearOffset(),
				maxBucketLengthMB);

		if (bucket != null) {
			DataInputStream ios = new DataInputStream(new FileInputStream(
					new File(this.getBaseDir(), path + this.zipIndexsuffix)));
			int len = ios.readInt();
			byte[] data = new byte[len];
			ios.read(data);
			this.zipIndex = (ArrayList<ZipIndexItem>) this.codec.decode(data);
			long off = findZipFileOffset(sequence);
			if (seq != -1L) {
				bucket.seek(off);
				while (true) {
					try {
						byte[] lookupdata = bucket.getNext();
						ChangedEvent event = (ChangedEvent) codec
								.decode(lookupdata);
						if (event.getSeq() == seq)
							break;
					} catch (EOFException e) {
						break;
					}
				}
			}
			/*
			 * try { if (seq != -1L && seq != -2L && !start) { bucket.getNext();
			 * } } catch (EOFException e) { // ignore }
			 */
		}

		return bucket;
	}

	public long findZipFileOffset(Sequence seq) {
		int size = this.zipIndex.size();
		for (int i = 0; i < size; i++) {
			if (this.zipIndex.get(i).getBenginseq() <= seq.longValue()
					&& this.zipIndex.get(i).getEndseq() >= seq.longValue()) {
				return this.zipIndex.get(i).getOffset();
			}
		}
		return -1;
	}
}
