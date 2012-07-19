/**
 * Project: puma-server
 * 
 * File Created at 2012-7-19
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
package com.dianping.puma.exception;

/**
 * TODO Comment of StorageClosedException
 * 
 * @author Leo Liang
 * 
 */
public class StorageClosedException extends StorageException {
	private static final long	serialVersionUID	= -1114590542695849132L;

	public StorageClosedException() {
		super();
	}

	public StorageClosedException(String message, Throwable t) {
		super(message, t);
	}

	public StorageClosedException(String message) {
		super(message);
	}
}
