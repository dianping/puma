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
package com.dianping.puma.storage.oldbucket;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

/**
 * 
 * @author Leo Liang
 * 
 */
public class DefaultHDFSConfigImpl implements HDFSConfig {

    private static final Logger logger     = Logger.getLogger(DefaultHDFSConfigImpl.class);
    private Configuration       hdfsConfig;
    private String              hdfsConfigStr;
    private FileSystem          fileSystem = null;
    private AtomicInteger       refCount   = new AtomicInteger(0);

    private Properties parseConfigStr(String hdfsConfigStr) {
        Properties prop = new Properties();
        if (StringUtils.isNotBlank(hdfsConfigStr)) {
            String[] lines = StringUtils.split(hdfsConfigStr, IOUtils.LINE_SEPARATOR);
            for (String line : lines) {
                String[] entry = StringUtils.split(line, '=');
                String key = entry[0];
                String value = entry[1];
                prop.setProperty(key, value);
            }
        } else {
            throw new IllegalArgumentException("hdfsConfigStr must not be empty!");
        }

        return prop;
    }

    @Override
    public void init() throws IOException {
        initHdfsConfiguration();
        fileSystem = FileSystem.get(this.hdfsConfig);
    }

    @Override
    public synchronized FileSystem getFileSystem() throws IOException {
        refCount.incrementAndGet();
        return fileSystem;
    }

    @Override
    public synchronized void close() throws IOException {
        if (refCount.decrementAndGet() == 0) {
            fileSystem.close();
        }
    }

    private void initHdfsConfiguration() {
        hdfsConfig = new Configuration();

        Properties prop = parseConfigStr(hdfsConfigStr);
        logger.info("hdfs properties: " + prop);
        for (String key : prop.stringPropertyNames()) {
            hdfsConfig.set(key, prop.getProperty(key));
        }

        UserGroupInformation.setConfiguration(hdfsConfig);
        try {
            SecurityUtil.login(hdfsConfig, prop.getProperty("keytabFileKey"), prop.getProperty("userNameKey"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void setHdfsConfigStr(String hdfsConfigStr) {
        this.hdfsConfigStr = hdfsConfigStr;
    }

}
