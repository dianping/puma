/**
 * Project: puma-server
 * 
 * File Created at 2012-7-7
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
package com.dianping.puma.consumeracceptor;

import com.dianping.puma.common.LifeCycle;

/**
 * TODO Comment of ConsumerAcceptor
 * 
 * @author Leo Liang
 * 
 */
public interface Acceptor extends LifeCycle {
	public String getAcceptorName();
}
