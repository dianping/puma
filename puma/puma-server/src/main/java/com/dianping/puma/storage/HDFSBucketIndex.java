/**
 * Project: puma-server
 * 
 * File Created at 2012-7-18
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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * @author Leo Liang
 */
public class HDFSBucketIndex extends AbstractBucketIndex {

    private static final Logger logger = Logger.getLogger(HDFSBucketIndex.class);

    private Configuration hdfsConfig;
    private FileSystem fileSystem;
    private String hdfsConfigStr;

    public void initHdfsConfiguration() {
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

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.storage.BucketIndex#init()
     */
    @Override
    public void start() throws IOException {
        initHdfsConfiguration();
        this.fileSystem = FileSystem.get(this.hdfsConfig);

        TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(new PathSequenceComparator());
        getIndex().set(newIndex);

        if (this.fileSystem.getFileStatus(new Path(this.getBaseDir())).isDir()) {

            FileStatus[] dirsStatus = this.fileSystem.listStatus(new Path(this.getBaseDir()));
            if (dirsStatus != null && dirsStatus.length != 0) {

                Path[] listedPaths = FileUtil.stat2Paths(dirsStatus);

                for (Path pathname : listedPaths) {

                    if (this.fileSystem.getFileStatus(pathname).isDir()) {
                        if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {

                            FileStatus[] status = this.fileSystem.listStatus(pathname);
                            Path[] listedFiles = FileUtil.stat2Paths(status);

                            for (Path subFile : listedFiles) {
                                if (subFile.getName().startsWith(getBucketFilePrefix())
                                        && StringUtils.isNumeric(subFile.getName().substring(getBucketFilePrefix().length()))) {
                                    String path = pathname.getName() + PATH_SEPARATOR + subFile.getName();
                                    newIndex.put(convertToSequence(path), path);
                                }
                            }
                        }
                    }

                }
            }
        }
        super.start();
    }

    @Override
    protected Bucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB) throws IOException {
        return new HDFSBucket(fileSystem, baseDir, path, startingSeq, !isMaster());
    }

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.storage.AbstractBucketIndex#close()
     */
    @Override
    public void stop() {
        super.stop();
        try {
            this.fileSystem.close();
        } catch (IOException e) {
            // ignore
        }

    }

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.storage.AbstractBucketIndex#doGetNextWriteBucket(java .lang.String, java.lang.String,
     * com.dianping.puma.storage.Sequence)
     */
    @Override
    protected Bucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence) throws IOException {
        return null;
    }

    @Override
    public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
        if (isMaster()) {
            return;
        }
        super.copyFromLocal(srcBaseDir, path);
        File localFile = new File(srcBaseDir, path);
        if (!localFile.exists()) {
            return;
        }
        Path destFile = new Path(this.getBaseDir(), path);

        GZIPOutputStream gos = null;

        try {
            gos = new GZIPOutputStream(fileSystem.create(destFile, true));
            FileUtils.copyFile(localFile, gos);
        } finally {

            if (gos != null) {
                try {
                    gos.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.dianping.puma.storage.AbstractBucketIndex#removeBucket(java.lang. String)
     */
    @Override
    public boolean removeBucket(String path) throws StorageClosedException {
        super.removeBucket(path);

        boolean deleted = false;

        try {
            Path p = new Path(getBaseDir(), path);
            if (this.fileSystem.exists(p)) {
                deleted = this.fileSystem.delete(p, false);
            }

            if (this.fileSystem.exists(p.getParent())) {
                FileStatus[] listStatus = this.fileSystem.listStatus(p.getParent());
                if (listStatus == null || listStatus.length == 0) {
                    this.fileSystem.delete(p.getParent(), false);
                }
            }

            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    public void setHdfsConfigStr(String hdfsConfigStr) {
        this.hdfsConfigStr = hdfsConfigStr;
    }

}
