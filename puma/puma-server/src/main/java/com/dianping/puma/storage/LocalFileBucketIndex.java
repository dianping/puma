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
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileBucketIndex extends AbstractBucketIndex {

	@Override
	protected Bucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB)
			throws IOException {
		return new LocalFileBucket(new File(baseDir, path), startingSeq, maxSizeMB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.AbstractBucketIndex#init()
	 */
	@Override
	public void start() throws IOException {
		File localBaseDir = new File(getBaseDir());
		if (!localBaseDir.exists()) {
			if (!localBaseDir.mkdirs()) {
				throw new RuntimeException("Failed to make dir for " + localBaseDir.getAbsolutePath());
			}
		}
		TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(new PathSequenceComparator());
		getIndex().set(newIndex);
		File[] dirs = localBaseDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {
						return true;
					}
				}
				return false;
			};
		});

		if (dirs != null) {
			for (File dir : dirs) {
				String[] subFiles = dir.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if (name.startsWith(getBucketFilePrefix())
								&& StringUtils.isNumeric(name.substring(getBucketFilePrefix().length()))) {
							return true;
						}
						return false;
					}
				});

				for (String subFile : subFiles) {
					String path = dir.getName() + PATH_SEPARATOR + subFile;
					newIndex.put(convertToSequence(path), path);
				}
			}
		}
		super.start();
	}

	@Override
	protected Bucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence)
			throws IOException {
		File bucketFile = new File(baseDir, bucketPath);

		if (!bucketFile.getParentFile().exists()) {
			if (!bucketFile.getParentFile().mkdirs()) {
				throw new IOException(String.format("Can't create writeBucket's parent(%s)!", bucketFile.getParent()));
			}
		}

		if (!bucketFile.createNewFile()) {
			throw new IOException(String.format("Can't create writeBucket(%s)!", bucketFile.getAbsolutePath()));
		} else {
			return new LocalFileBucket(bucketFile, startingSequence, getMaxBucketLengthMB());
		}

	}

	public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
		super.copyFromLocal(srcBaseDir, path);
		File localFile = new File(srcBaseDir, path);
		if (!localFile.exists()) {
			return;
		}
		File destFile = new File(this.getBaseDir(),path);
		if (!destFile.getParentFile().exists()) {
			if (!destFile.getParentFile().mkdirs()) {
				throw new IOException(String.format(
						"Can't create writeBucket's parent(%s)!",
						destFile.getParent()));
			}
		}
		zipIndex.clear();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		RandomAccessFile destFileAcess = new RandomAccessFile(destFile, "rw");
		RandomAccessFile localFileAcess = new RandomAccessFile(localFile, "rw");
		bos.write(ByteArrayUtils.intToByteArray("ZIPFORMAT".length()));
		bos.write("ZIPFORMAT".getBytes());
		destFileAcess.write(bos.toByteArray());
		while(localFileAcess.getFilePointer() + 4 < localFileAcess.length()){
			byte[] data = readAndZip(localFileAcess, localFile, destFileAcess.getFilePointer());
			bos.reset();
			bos.write(ByteArrayUtils.intToByteArray(data.length));
			bos.write(data);
			destFileAcess.write(bos.toByteArray());
		}
		OutputStream ios = new FileOutputStream(new File(this.getBaseDir(),
				path + this.zipIndexsuffix));
		if(zipIndex.isEmpty())
			return;
		byte[] index = this.codec.encode(zipIndex);
		bos.reset();
		bos.write(ByteArrayUtils.intToByteArray(index.length));
		bos.write(index);
		ios.write(bos.toByteArray());
		ios.close();
		destFileAcess.close();
		bos.close();
	}

	@Override
	public boolean removeBucket(String path) throws StorageClosedException {
		super.removeBucket(path);
		File file = new File(getBaseDir(), path);
		boolean deleted = false;
		if (file.exists()) {
			deleted = file.delete();
			if(deleted){
				File index = new File(getBaseDir(), path + this.zipIndexsuffix);
				index.delete();
			}
				
		}

		if (file.getParentFile().exists()) {
			String[] subFiles = file.getParentFile().list();
			if (subFiles == null || subFiles.length == 0) {
				file.getParentFile().delete();
			}
		}
		return deleted;
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
			bucket.seek(offset);
			try {
				if (seq != -1L && seq != -2L && !start) {
					bucket.getNext();
				}
			} catch (EOFException e) {
				// ignore
			}
		}

		return bucket;
	}
}
