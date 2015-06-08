/**
 * Project: puma-client
 * 
 * File Created at 2012-7-8
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
package com.dianping.puma.api.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import com.dianping.puma.api.Configuration;
import com.dianping.puma.core.constant.SubscribeConstant;

/**
 * 基于MMap实现的seq文件操作类
 * 
 * @author Leo Liang
 * 
 */
public class FileSequenceHolder implements SequenceHolder {
	private static final int MAX_FILE_LENGTH = 100;
	private final String seqFileBase = "/data/appdatas/puma/";
	private Configuration config;
	private MappedByteBuffer buf;
	private RandomAccessFile file;
	private long seq;
	private static final byte[] BUF_MASK = new byte[MAX_FILE_LENGTH];

	public FileSequenceHolder(Configuration config) {
		this.config = config;

		String filePath = getSeqConfigFilePath();
		File seqFile = new File(seqFileBase, filePath);
		filePath = seqFile.getAbsolutePath();
		initSeq(seqFile);
		ensureFile(filePath);

		try {
			this.file = new RandomAccessFile(new File(filePath), "rwd");
			buf = this.file.getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_LENGTH);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @param file
	 */
	private void initSeq(File file) {
		if (!file.exists()) {
			this.seq = SubscribeConstant.SEQ_FROM_OLDEST;
			return;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String readSeq = br.readLine();
			if (readSeq == null) {
				this.seq = SubscribeConstant.SEQ_FROM_OLDEST;
				return;
			} else {
				this.seq = Long.parseLong(readSeq);
				return;
			}
		} catch (Exception e) {
			this.seq = SubscribeConstant.SEQ_FROM_OLDEST;
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private String getSeqConfigFilePath() {
		return (new StringBuilder())
				.append("seq")
				.append("-")
				.append(config.getName())
				.append("-")
				.append(config.getTarget())
				.append(".conf")
				.toString();
	}

	public synchronized void saveSeq(long seq) {
		buf.position(0);
		buf.put(BUF_MASK);
		buf.position(0);
		buf.put(String.valueOf(seq).getBytes());
		buf.put("\n".getBytes());
		this.seq = seq;
	}

	public synchronized long getSeq() {
		return seq;
	}

	private void ensureFile(String fileName) {
		File path = new File(fileName);
		if (!path.getParentFile().exists()) {
			if (!path.getParentFile().mkdirs()) {
				throw new RuntimeException("can not create dir: " + path.getParent());
			}
		}
		if (!path.exists()) {
			try {
				if (!path.createNewFile()) {
					throw new RuntimeException("can not create file: " + path);
				}
			} catch (IOException e) {
				throw new RuntimeException("can not create file: " + path, e);
			}
		}

	}
}
