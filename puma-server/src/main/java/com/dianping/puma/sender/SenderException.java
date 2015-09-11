/**
 * Project: puma-server
 * 
 * File Created at 2012-7-31
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
package com.dianping.puma.sender;

/**
 * 
 * @author Leo Liang
 * 
 */
public class SenderException extends Exception {

	private static final long	serialVersionUID	= -3939678557441421475L;

	public SenderException() {
		super();
	}

	public SenderException(String message, Throwable t) {
		super(message, t);
	}

	public SenderException(String message) {
		super(message);
	}
}
