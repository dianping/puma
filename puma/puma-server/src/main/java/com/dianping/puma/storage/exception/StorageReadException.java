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
package com.dianping.puma.storage.exception;

/**
 * TODO Comment of StorageInitException
 * 
 * @author Leo Liang
 * 
 */
public class StorageReadException extends StorageException {

	private static final long	serialVersionUID	= 6449475196273722911L;

	public StorageReadException() {
		super();
	}

	public StorageReadException(String message, Throwable t) {
		super(message, t);
	}

	public StorageReadException(String message) {
		super(message);
	}
}
