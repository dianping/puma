/**
 * Project: puma-server
 * 
 * File Created at 2013-6-25
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
package com.dianping.puma.storage.bucket;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;

/**
 * @author Leo Liang
 * 
 */
public interface HDFSConfig {
    public FileSystem getFileSystem() throws IOException;

    public void close() throws IOException;

    public void init() throws IOException;
}
