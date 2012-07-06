/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-7-3
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
package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

/**
 * @author Leo Liang
 * 
 */
public interface Bucket {
	public long getStartingSequece();
	
	public void append(ChangedEvent event) throws IOException;

	public ChangedEvent getNext() throws IOException;

	public void seek(int offset) throws IOException;

	public void close() throws IOException;

}
