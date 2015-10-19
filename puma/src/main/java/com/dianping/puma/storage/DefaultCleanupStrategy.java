///**
// * Project: puma-server
// *
// * File Created at 2012-8-6
// * $Id$
// *
// * Copyright 2010 dianping.com.
// * All rights reserved.
// *
// * This software is the confidential and proprietary information of
// * Dianping Company. ("Confidential Information").  You shall not
// * disclose such Confidential Information and shall use it only in
// * accordance with the terms of the license agreement you entered into
// * with dianping.com.
// */
//package com.dianping.puma.storage;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import com.dianping.puma.storage.oldindex.WriteIndexManager;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.dianping.cat.Cat;
//import com.dianping.puma.storage.oldbucket.DataBucketManager;
//import com.dianping.puma.storage.exception.StorageClosedException;
//
///**
// *
// * @author Leo Liang
// *
// */
//public class DefaultCleanupStrategy implements CleanupStrategy {
//
//	private static final Logger logger = LoggerFactory.getLogger(DefaultCleanupStrategy.class);
//
//	private int preservedDay = 14;
//
//	private List<String> toBeDeleteBuckets = new ArrayList<String>();
//
//	@SuppressWarnings("rawtypes")
//	private List<WriteIndexManager> dataIndexes = new ArrayList<WriteIndexManager>();
//
//	public void setPreservedDay(int preservedDay) {
//		this.preservedDay = preservedDay;
//	}
//
//	@SuppressWarnings("rawtypes")
//	public void addDataIndex(WriteIndexManager index) {
//		this.dataIndexes.add(index);
//	}
//
//	@SuppressWarnings("rawtypes")
//	@Override
//	public void cleanup(DataBucketManager index) {
//		try {
//			toBeDeleteBuckets.addAll(index.bulkGetRemainNDay(preservedDay));
//
//			if (!toBeDeleteBuckets.isEmpty()) {
//				index.remove(toBeDeleteBuckets);
//				for (String path : toBeDeleteBuckets) {
//					if (dataIndexes != null && !dataIndexes.isEmpty()) {
//						for (WriteIndexManager dataIndex : dataIndexes) {
//							try {
//								String indexName = path.replace('/', '-');
//								dataIndex.removeByL2IndexName(indexName);
//
//								logger.info("cleanUp l1Index " + indexName);
//							} catch (IOException e) {
//								Cat.logError(e);
//								logger.error(e.getMessage());
//							}
//						}
//					}
//				}
//
//				Iterator<String> iterator = toBeDeleteBuckets.iterator();
//				while (iterator.hasNext()) {
//					String path = iterator.next();
//					if (StringUtils.isNotBlank(path)) {
//						if (index.removeBucket(path)) {
//							iterator.remove();
//						}
//					}
//				}
//			}
//		} catch (StorageClosedException e) {
//			// ignore
//		}
//	}
//}
