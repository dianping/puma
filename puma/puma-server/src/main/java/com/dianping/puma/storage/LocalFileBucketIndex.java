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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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
        return new LocalFileBucket(new File(baseDir, path), startingSeq, maxSizeMB);
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
        TreeMap<Sequence, String> newIndex = new TreeMap<Sequence, String>();
        getIndex().set(new TreeMap<Sequence, String>());
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
        getIndex().set(newIndex);
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
            return new LocalFileBucket(bucketFile, startingSequence, getMaxBucketLengthMB());
        }

    }

    public void copyFromLocal(String srcBaseDir, String path) throws IOException, StorageClosedException {
        super.copyFromLocal(srcBaseDir, path);
        File localFile = new File(srcBaseDir, path);
        if (!localFile.exists()) {
            return;
        }
        File destFile = new File(this.getBaseDir(), path);
        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                throw new IOException(String.format("Can't create writeBucket's parent(%s)!", destFile.getParent()));
            }
        }
        RandomAccessFile localFileAcess = new RandomAccessFile(localFile, "rw");
        RandomAccessFile destFileAcess = new RandomAccessFile(destFile, "rw");
        OutputStream destIndex = new FileOutputStream(new File(this.getBaseDir(), path + this.zipIndexsuffix));
        this.compressor.compress(localFileAcess, destFileAcess, destIndex);
        localFileAcess.close();
        destFileAcess.close();
        destIndex.close();
    }

    @Override
    public boolean removeBucket(String path) throws StorageClosedException {
        super.removeBucket(path);
        File file = new File(getBaseDir(), path);
        boolean deleted = false;
        if (file.exists()) {
            deleted = file.delete();
            if (deleted) {
                File index = new File(getBaseDir(), path + this.zipIndexsuffix);
                index.delete();
            }

        }

        if (file.getParentFile().exists()) {
            String[] subFiles = file.getParentFile().list();
            if (subFiles == null || subFiles.length == 0) {
                file.getParentFile().delete();
            }
        }
        return deleted;
    }

    @Override
    public ArrayList<ZipIndexItem> readZipIndex(String baseDir, String path) throws IOException {
        Properties properties = new Properties();
        DataInputStream ios = new DataInputStream(new FileInputStream(new File(baseDir, path)));
        properties.load(ios);
        ios.close();
        ArrayList<ZipIndexItem> results = new ArrayList<ZipIndexItem>();
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            ZipIndexItem item = new ZipIndexItem(Long.valueOf(key.substring(0, key.indexOf(ZIPINDEX_SEPARATOR)))
                    .longValue(), Long.valueOf(key.substring(key.indexOf(ZIPINDEX_SEPARATOR) + 1)).longValue(),
                    Long.valueOf(properties.getProperty(key)));
            results.add(item);
        }
        return results;
    }
}
