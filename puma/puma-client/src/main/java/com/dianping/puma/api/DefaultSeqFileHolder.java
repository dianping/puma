/**
 * Project: puma-client
 * 
 * File Created at 2012-7-5
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
package com.dianping.puma.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * TODO Comment of DefaultSeqFileHolder
 * 
 * @author Leo Liang
 * 
 */
public class DefaultSeqFileHolder implements SeqFileHolder {
	private static final Logger	log					= Logger.getLogger(DefaultSeqFileHolder.class);
	private static final String	DEFAULT_SEQFILEBASE	= "/data/applogs/puma/";
	private String				seqFileBase;
	private Configuration		config;

	/**
	 * @param seqFileBase
	 * @param config
	 */
	public DefaultSeqFileHolder(String seqFileBase, Configuration config) {
		super();
		this.seqFileBase = seqFileBase;
		this.config = config;
	}

	public DefaultSeqFileHolder(Configuration config) {
		this(DEFAULT_SEQFILEBASE, config);
	}

	/**
	 * @param seqFileBase
	 *            the seqFileBase to set
	 */
	public void setSeqFileBase(String seqFileBase) {
		this.seqFileBase = seqFileBase;
	}

	public String getSeqConfigFilePath() {
		String[] hostArr = config.getHost().split("\\.");
		StringBuilder path = new StringBuilder(seqFileBase);
		path.append("seq-");
		for (String hostPart : hostArr) {
			path.append(hostPart).append("-");
		}
		path.append(config.getPort()).append(".conf");
		return path.toString();
	}

	public void saveSeq(long seq) {
		File file = new File(getSeqConfigFilePath());

		ensureDir(file);

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(String.valueOf(seq));
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			log.error("Save seq failed.", e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public long getSeq() {
		File file = new File(getSeqConfigFilePath());

		if (!file.exists()) {
			return -1L;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String seq = br.readLine();
			if (seq == null) {
				return -1L;
			} else {
				return Long.parseLong(seq);
			}
		} catch (Exception e) {
			log.error("Read seq failed.", e);
			return -1L;
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

	private File ensureDir(File dir) {

		if (!dir.exists()) {
			if (!dir.getParentFile().mkdirs()) {
				log.error("Can not mkdirs. Path: " + dir.getParent());
			}

		}
		return dir;
	}

}
