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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 
 * @author Leo Liang
 * 
 */
public class LocalFileBucketIndex extends AbstractBucketIndex {

    @Override
    protected Bucket doGetReadBucket(String baseDir, String path, Sequence startingSeq, int maxSizeMB)
            throws IOException {
        return new LocalFileBucket(new File(baseDir, path), startingSeq, maxSizeMB, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.storage.AbstractBucketIndex#init()
     */
    @Override
    public void start() throws IOException {
        File localBaseDir = new File(getBaseDir());
        if (!localBaseDir.exists()) {
            if (!localBaseDir.mkdirs()) {
                throw new RuntimeException("Failed to make dir for " + localBaseDir.getAbsolutePath());
            }
        }
        TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>(new PathSequenceComparator());
        getIndex().set(newIndex);
        File[] dirs = localBaseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {
                        return true;
                    }
                }
                return false;
            };
        });

        if (dirs != null) {
            for (File dir : dirs) {
                String[] subFiles = dir.list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.startsWith(getBucketFilePrefix())
                                && StringUtils.isNumeric(name.substring(getBucketFilePrefix().length()))) {
                            return true;
                        }
                        return false;
                    }
                });

                for (String subFile : subFiles) {
                    String path = dir.getName() + PATH_SEPARATOR + subFile;
                    newIndex.put(convertToSequence(path), path);
                }
            }
        }
        super.start();
    }

    @Override
    protected Bucket doGetNextWriteBucket(String baseDir, String bucketPath, Sequence startingSequence)
            throws IOException {
        File bucketFile = new File(baseDir, bucketPath);

        if (!bucketFile.getParentFile().exists()) {
            if (!bucketFile.getParentFile().mkdirs()) {
                throw new IOException(String.format("Can't create writeBucket's parent(%s)!", bucketFile.getParent()));
            }
        }

        if (!bucketFile.createNewFile()) {
            throw new IOException(String.format("Can't create writeBucket(%s)!", bucketFile.getAbsolutePath()));
        } else {
            return new LocalFileBucket(bucketFile, startingSequence, getMaxBucketLengthMB(), bucketPath);
        }

    }

    public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
        super.copyFromLocal(srcBaseDir, path);
        File localFile = new File(srcBaseDir, path);
        if (!localFile.exists()) {
            return;
        }

        FileUtils.copyFile(localFile, new File(getBaseDir(), path));
    }

    @Override
    public boolean removeBucket(String path) throws StorageClosedException {
        super.removeBucket(path);
        File file = new File(getBaseDir(), path);
        boolean deleted = false;
        if (file.exists()) {
            deleted = file.delete();
        }

        if (file.getParentFile().exists()) {
            String[] subFiles = file.getParentFile().list();
            if (subFiles == null || subFiles.length == 0) {
                file.getParentFile().delete();
            }
        }
        return deleted;
    }
}
