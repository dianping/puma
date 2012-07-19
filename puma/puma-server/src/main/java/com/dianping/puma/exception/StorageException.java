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
 * TODO Comment of StorageException
 * 
 * @author Leo Liang
 * 
 */
public class StorageException extends Exception {

	private static final long	serialVersionUID	= -3955468875272140081L;

	public StorageException() {
		super();
	}

	public StorageException(String message, Throwable t) {
		super(message, t);
	}

	public StorageException(String message) {
		super(message);
	}
}
