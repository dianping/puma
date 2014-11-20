/**
 * Project: ${swallow-client.aid}
 * 
 * File Created at 2011-7-29
 * $Id$
 * 
 * Copyright 2011 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.core.util;

/***
 * 从MQ拉取消息时采取的动作
 * @author qing.gu
 *
 */
public interface PullStrategy {

	/***
	 * 没有拉到消息
	 * @throws InterruptedException
	 */
	long fail(boolean shouldSleep) throws InterruptedException;
	
	/***
	 * 拉到了消息
	 */
	void succeess();
	
}
