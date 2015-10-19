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
package com.dianping.puma.storage.oldbucket;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * @author Leo Liang
 */
public class HDFSDataBucketManager extends com.dianping.puma.storage.oldbucket.AbstractDataBucketManager {

    private FileSystem fileSystem;
    private HDFSConfig hdfsConfig;

    /**
     * @param hdfsConfig
     *            the hdfsConfig to set
     */
    public void setHdfsConfig(HDFSConfig hdfsConfig) {
        this.hdfsConfig = hdfsConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.storage.BucketIndex#init()
     */
    @Override
    public void start() throws IOException {

        this.fileSystem = hdfsConfig.getFileSystem();

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
                                        && StringUtils.isNumeric(subFile.getName().substring(
                                                getBucketFilePrefix().length()))) {
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
    protected ReadDataBucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB)
            throws IOException {
        return new HDFSDataBucket(fileSystem, baseDir, path, startingSeq, !isMaster());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.storage.AbstractBucketIndex#close()
     */
    @Override
    public void stop() {
        super.stop();
        try {
            hdfsConfig.close();
        } catch (IOException e) {
            // ignore
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.puma.storage.AbstractBucketIndex#doGetNextWriteBucket(java
     * .lang.String, java.lang.String, com.dianping.puma.storage.Sequence)
     */
    @Override
    protected ReadDataBucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence)
            throws IOException {
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
     * 
     * @see
     * com.dianping.puma.storage.AbstractBucketIndex#removeBucket(java.lang.
     * String)
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

}
