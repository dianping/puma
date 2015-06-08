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

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.exception.CacheException;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.api.Configuration;
import com.dianping.puma.api.SpringContainer;

/**
 * 基于Memcached实现的seq文件操作类
 * 
 * @author xiang.wu
 * 
 */
public class MemcachedSequenceHolder implements SequenceHolder {
	private Configuration config;
	private long seq;
	private static SpringContainer SPRING_CONTAINER = new SpringContainer();
	private CacheService cacheService;
	private CacheKey cacheKey = null;
	private static Logger logger = LoggerFactory.getLogger(MemcachedSequenceHolder.class);
	private SequenceHolder fileSequenceHolder = null;

	public MemcachedSequenceHolder(Configuration config) {
		this.config = config;
		SPRING_CONTAINER.start();
		cacheService = (CacheService) SPRING_CONTAINER.getBean("cacheService");
		String key = (new StringBuilder())
				.append("seq")
				.append("-")
				.append(config.getName())
				.append("-")
				.append(config.getTarget())
				.toString();
		cacheKey = new CacheKey("puma-seq", key);
		logger.info("puma cache key:" + key);

		try {
			fileSequenceHolder = new FileSequenceHolder(config);
		} catch (Throwable e) {
			logger.warn("error while initializing file sequence:", e);
		}
		initSeq();
	}

	private void initSeq() {
		try {
			Cat.logEvent("Puma.cache.seq", cacheKey.toString(), Message.SUCCESS, "");
			Object seqFromCache = cacheService.get(cacheKey);
			if (seqFromCache != null) {
				long s = Long.valueOf(seqFromCache.toString());
				this.seq = s;
				logger.warn("read seq from cache:" + this.seq);
				return;
			}
		} catch (RuntimeException e) {
			logger.warn("error while reading seq from cache:" + cacheKey + ", caused by:" + e.getMessage());
		}
		if (fileSequenceHolder != null) {
			this.seq = fileSequenceHolder.getSeq();
		}
		logger.warn("use seq stored in local seq file:" + this.seq);
	}

	public synchronized void saveSeq(long seq) {
		try {
			fileSequenceHolder.saveSeq(seq);
		} catch (Throwable e) {
		}
		try {
			cacheService.set(cacheKey, seq);
		} catch (CacheException e) {
			logger.warn("save seq error:" + seq + ", caused by:" + e.getMessage());
		} catch (TimeoutException e) {
			logger.warn("save seq timeout:" + seq + ", caused by:" + e.getMessage());
		}
		this.seq = seq;
	}

	public synchronized long getSeq() {
		return seq;
	}

}
