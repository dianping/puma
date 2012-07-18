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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DefaultArchiveStrategy implements ArchiveStrategy {
	private static final Logger	log					= Logger.getLogger(DefaultArchiveStrategy.class);
	private List<String>		toBeArchiveBuckets	= new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.puma.storage.ArchiveTask#archive(com.dianping.puma.storage
	 * .BucketIndex, com.dianping.puma.storage.BucketIndex, int)
	 */
	@Override
	public void archive(BucketIndex masterIndex, BucketIndex slaveIndex, int masterRemainFileCount) {
		if (masterIndex.size() > masterRemainFileCount) {
			toBeArchiveBuckets.addAll(masterIndex.bulkGetRemainN(masterRemainFileCount));
		}

		if (toBeArchiveBuckets.size() > 0) {
			List<String> copiedFiles = new ArrayList<String>();

			Iterator<String> iterator = toBeArchiveBuckets.iterator();
			while (iterator.hasNext()) {
				String path = iterator.next();
				if (StringUtils.isNotBlank(path)) {
					if (doArchive(masterIndex.getBaseDir(), path, slaveIndex, copiedFiles)) {
						iterator.remove();
					}
				}
			}

			slaveIndex.add(copiedFiles);
			masterIndex.remove(copiedFiles);

			cleanUpLocalFiles(masterIndex.getBaseDir(), copiedFiles);
		}
	}

	private void cleanUpLocalFiles(String baseDir, List<String> paths) {
		for (String path : paths) {
			File localFile = new File(baseDir, path);
			localFile.delete();

			File parent = localFile.getParentFile();
			if (parent != null) {
				String[] subFiles = parent.list();
				if (subFiles != null && subFiles.length == 0) {
					if (!parent.delete()) {
						log.warn("Delete folder(" + parent.getAbsolutePath() + ") failed.");
					}
				}
			}
		}
	}

	private boolean doArchive(String baseDir, String path, BucketIndex dest, List<String> copiedFiles) {
		try {
			dest.copyFromLocal(baseDir, path);
			copiedFiles.add(path);

			return true;
		} catch (Exception e) {
			log.warn("Archive failed. path: " + path, e);
		}

		return false;
	}

}
