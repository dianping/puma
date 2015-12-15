/**
 * Project: puma-server
 * <p/>
 * File Created at 2012-8-3
 * $Id$
 * <p/>
 * Copyright 2010 dianping.com.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.parser.meta;

/**
 *
 * @author Leo Liang
 *
 */
public interface TableMetaInfoFetcher {

    void refreshTableMeta(String database, String table);

    void refreshTableMetas();

    TableMetaInfo getTableMetaInfo(String database, String table);
}
