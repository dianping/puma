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

import com.dianping.puma.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DefaultArchiveStrategy implements ArchiveStrategy {
	private static final Logger	log					= Logger.getLogger(DefaultArchiveStrategy.class);
	private List<String>		toBeArchiveBuckets	= new ArrayList<String>();
	private List<String>		toBeDeleteBuckets	= new ArrayList<String>();

	@Override
	public void archive(BucketIndex masterIndex, BucketIndex slaveIndex, int masterRemainFileCount) {
		try {
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

				toBeDeleteBuckets.addAll(copiedFiles);
				cleanUpLocalFiles(masterIndex.getBaseDir(), toBeDeleteBuckets);
			}
		} catch (StorageClosedException e) {
			// ignore
		}
	}

	private void cleanUpLocalFiles(String baseDir, List<String> paths) {
		Iterator<String> iterator = paths.iterator();
		while (iterator.hasNext()) {
			File localFile = new File(baseDir, iterator.next());
			if (localFile.delete()) {
				iterator.remove();
			}

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
