/**
 * Project: puma-client
 * 
 * File Created at 2012-7-5
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
package com.dianping.puma.api;

/**
 * TODO Comment of SeqFileHolder
 * 
 * @author Leo Liang
 * 
 */
public interface SeqFileHolder {

	public void saveSeq(long seq);

	public long getSeq();

}
