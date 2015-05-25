/**
 * Project: puma-server
 * 
 * File Created at 2012-8-3
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
package com.dianping.puma.datahandler;

import com.dianping.puma.core.event.DdlEvent;

/**
 * TODO Comment of TableMetasInfoFetcher
 * 
 * @author Leo Liang
 * 
 */
public interface TableMetaInfoFetcher {

	void refreshTableMeta(DdlEvent ddlEvent, boolean isRefresh);

	TableMetaInfo getTableMetaInfo(String database, String table);
	
}
