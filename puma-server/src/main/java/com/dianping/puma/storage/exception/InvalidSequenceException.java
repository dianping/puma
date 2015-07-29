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
 * 
 * @author Leo Liang
 * 
 */
public class InvalidSequenceException extends StorageException {

	private static final long	serialVersionUID	= 1314449301630651950L;

	public InvalidSequenceException() {
		super();
	}

	public InvalidSequenceException(String message, Throwable t) {
		super(message, t);
	}

	public InvalidSequenceException(String message) {
		super(message);
	}
}
