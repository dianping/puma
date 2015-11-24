package com.dianping.puma.utils;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZipUtilsTest {

    private File tempDir;

    @Before
    public void before() {
        tempDir = new File(System.getProperty("java.io.tmpdir"), "puma");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("failed to create temp directory.");
        }
    }

    @Test
    public void testCheckGZip_0() throws Exception {
        // Case 0: empty file.
        File file_0 = new File(tempDir, "file_0");
        if (!file_0.createNewFile()) {
            throw new RuntimeException("failed to create file `file_0`.");
        }
        assertFalse(ZipUtils.checkGZip(file_0));
    }

    @Test
    public void testCheckGZip_1() throws IOException {
        // Case 0: empty normal file.
        File file_0 = new File(tempDir, "file_0");
        if (!file_0.createNewFile()) {
            throw new RuntimeException("failed to create file `file_0`.");
        }
        new FileOutputStream(file_0);
        assertFalse(ZipUtils.checkGZip(file_0));

        // Case 1: contented normal file.
        File file_1 = new File(tempDir, "file_1");
        if (!file_1.createNewFile()) {
            throw new RuntimeException("failed to create file `file_1`.");
        }
        OutputStream os = new FileOutputStream(file_1);
        os.write(10);
        os.write(100);
        os.write(1000);
        assertFalse(ZipUtils.checkGZip(file_1));
    }

    @Test
    public void testCheckGZip_2() throws IOException {
        // Case 0: empty gzip file.
        File file_0 = new File(tempDir, "file_0");
        if (!file_0.createNewFile()) {
            throw new RuntimeException("failed to create file `file_0`.");
        }
        new GZIPOutputStream(new FileOutputStream(file_0));
        assertTrue(ZipUtils.checkGZip(file_0));

        // Case 1: contented gzip file.
        File file_1 = new File(tempDir, "file_1");
        if (!file_1.createNewFile()) {
            throw new RuntimeException("failed to create file `file_1`.");
        }
        OutputStream os = new GZIPOutputStream(new FileOutputStream(file_1));
        os.write(10);
        os.write(100);
        os.write(1000);
        assertTrue(ZipUtils.checkGZip(file_1));
    }

    @After
    public void after() {
        try {
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException ignore) {
        }
    }
}