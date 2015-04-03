package com.dianping.puma.syncserver.job.executor;

import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.model.BinlogInfo;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Dozer @ 2015-02
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ShardDumpTaskExecutorTest {
    ShardDumpTask task;
    ShardDumpTaskExecutor target;
    ShardDumpTaskExecutor.DumpWorker dumpWorker;
    static final String FILE_NAME = "/tmp/ShardDumpTaskExecutorTest.tmp";
    File file = new File(FILE_NAME);

    @Before
    public void setUp() throws Exception {
        task = new ShardDumpTask();
        task.setTableName("test");
        task.setIndexColumnName("id");
        target = spy(new ShardDumpTaskExecutor(task));
        dumpWorker = spy(target.new DumpWorker());

        doReturn(FILE_NAME).when(target).getDumpFile(anyInt());

        deleteFile();
    }

    @After
    public void tearDown() throws Exception {
        deleteFile();
    }

    @Test
    public void testGetPositionFromContent() throws Exception {
        doReturn("-- CHANGE MASTER TO MASTER_LOG_FILE='mysqlbin.log.000010', MASTER_LOG_POS=91841517;")
                .when(dumpWorker).readFirstLine(anyInt());
        doNothing().when(dumpWorker).checkAndUpdateBinlogInfo("mysqlbin.log.000010", 91841517);

        dumpWorker.checkAndUpdateBinlogInfo(1);
        verify(dumpWorker, times(1)).readFirstLine(1);
        verify(dumpWorker, times(1)).checkAndUpdateBinlogInfo("mysqlbin.log.000010", 91841517);
    }

    @Test
    public void testBinlogNotNeedToUpdate() throws Exception {
        BinlogInfo info = new BinlogInfo();
        info.setBinlogFile("xxxx.0010");
        info.setBinlogPosition(10l);
        this.task.setBinlogInfo(info);

        doNothing().when(target).saveTask();

        dumpWorker.checkAndUpdateBinlogInfo("xxxx.0010", 11l);
        verify(target, times(0)).saveTask();

        dumpWorker.checkAndUpdateBinlogInfo("xxxx.0011", 1l);
        verify(target, times(0)).saveTask();
    }

    @Test
    public void testBinlogNeedToUpdate() throws Exception {
        BinlogInfo info = new BinlogInfo();
        info.setBinlogFile("xxxx.0010");
        info.setBinlogPosition(10l);
        this.task.setBinlogInfo(info);

        doNothing().when(target).saveTask();

        dumpWorker.checkAndUpdateBinlogInfo("xxxx.0010", 9l);
        verify(target, times(1)).saveTask();

        dumpWorker.checkAndUpdateBinlogInfo("xxxx.0009", 100l);
        verify(target, times(2)).saveTask();
    }

    @Test
    public void testcheckData_no_file() throws Exception {
        Assert.assertFalse(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    @Test
    public void testcheckData_empty_file() throws Exception {
        file.createNewFile();

        Assert.assertTrue(file.exists());
        Assert.assertFalse(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    @Test
    public void testcheckData_file_with_comment() throws Exception {
        file.createNewFile();
        writeLine("--XXXXXXXXXX");

        Assert.assertTrue(file.length() > 0);
        Assert.assertFalse(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    @Test
    public void testcheckData_file_with_insert() throws Exception {
        file.createNewFile();
        writeLine("INSERT INTO XXX");

        Assert.assertTrue(file.length() > 0);
        Assert.assertTrue(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    @Test
    public void testcheckData_file_with_insert_and_comment() throws Exception {
        file.createNewFile();
        writeLine("--XXXXXXXXXX");
        writeLine("INSERT INTO XXX");

        Assert.assertTrue(file.length() > 0);
        Assert.assertTrue(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    @Test
    public void testcheckData_file_with_big_file() throws Exception {
        file.createNewFile();

        while (file.length() < 1024 * 8) {
            writeLine("--XXXXXXXXXX");
        }

        Assert.assertTrue(file.length() > 0);
        Assert.assertTrue(dumpWorker.checkHasData(1));
        verify(target, times(1)).getDumpFile(anyInt());
    }

    private void writeLine(String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(content);
        writer.newLine();
        writer.flush();
        writer.close();
    }

    private void deleteFile() {
        if (file.exists()) {
            file.delete();
        }
    }
}