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
package com.dianping.puma.sender.dispatcher;

/**
 * TODO Comment of DispatcherException
 * 
 * @author Leo Liang
 * 
 */
public class DispatcherException extends Exception {

	private static final long	serialVersionUID	= -3753972308788488772L;

	public DispatcherException() {
		super();
	}

	public DispatcherException(String message, Throwable t) {
		super(message, t);
	}

	public DispatcherException(String message) {
		super(message);
	}
}
