package com.dianping.puma.storage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StorageBaseTest {

    protected File testDir;

    protected void setUp() throws Exception {
        testDir = new File(FileUtils.getTempDirectory(), "test-dir");
        System.out.println(testDir.getAbsoluteFile());
        deleteDirectory(testDir);
        createDirectory(testDir);
    }

    protected void tearDown() throws Exception {
        deleteDirectory(testDir);
    }

    protected void createDirectory(File directory) throws IOException {
        FileUtils.forceMkdir(directory);
        FileUtils.cleanDirectory(directory);
    }

    protected void deleteDirectory(File directory) throws IOException {
        try {
            FileUtils.forceDelete(directory);
        } catch (FileNotFoundException ignore) {
        }
    }

    protected void createFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("failed to create file parent directories.");
        }

        if (!file.createNewFile()) {
            throw new IOException("file already exists.");
        }
    }

    protected void deleteFile(File file) throws IOException {
        try {
            FileUtils.forceDelete(file);
        } catch (FileNotFoundException ignore) {
        }
    }
}
