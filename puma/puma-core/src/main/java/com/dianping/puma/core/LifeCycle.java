/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-6-25
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
package com.dianping.puma.core;

/**
 * TODO Comment of LifeCycle
 * 
 * @author Leo Liang
 * 
 */
public interface LifeCycle<T extends Exception> {
	public void start() throws T;

	public void stop() throws T;
}
