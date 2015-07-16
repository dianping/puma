package com.dianping.puma.storage.bucket;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.bucket.LocalFileDataBucket;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.exception.StorageClosedException;

public class LocalBucketTest {

    protected LocalFileDataBucket localFileBucket;
    protected File            work    = null;
    protected File            zipWork = null;
    protected File            zipDir  = null;

    @BeforeClass
    public static void init() {

    }

    @Before
    public void before() {
        work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-0");
        work.getParentFile().mkdirs();

        try {
            if (work.createNewFile())
                System.out.println("create a file!");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        zipWork = new File(System.getProperty("java.io.tmpdir", "."), "Puma/zip/20120710/bucket-0");
        zipDir = zipWork.getParentFile();
        zipDir.mkdirs();

        Sequence sequence = new Sequence(120710, 0, 0,0);

        try {
            localFileBucket = new LocalFileDataBucket(work, sequence, 10, "20120710/bucket-0", false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testgetStartingSequece() {
        Sequence seq = localFileBucket.getStartingSequece();

        Assert.assertEquals(0, seq.getNumber());
        Assert.assertEquals(0, seq.getOffset());
        Assert.assertEquals(120710, seq.getCreationDate());

    }

    @Test
    public void testAppend() {
        String event = "Write an event to the file";
        byte[] data = event.getBytes();
        try {
            localFileBucket.append(data);
        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader input = new BufferedReader(new FileReader(work));
            String s = input.readLine();
            input.close();
            Assert.assertEquals(event, s);
            Assert.assertEquals(data.length, new Sequence(localFileBucket.getCurrentWritingSeq()).getOffset());
            Assert.assertEquals(data.length + this.localFileBucket.getStartingSequece().longValue(),
                    localFileBucket.getCurrentWritingSeq().longValue());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetNext() {
        Sequence seq = localFileBucket.getStartingSequece();
        DdlEvent event = new DdlEvent();
        event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
        event.setDatabase("cat");
        event.setExecuteTime(0);
        event.setSeq(seq.longValue());
        event.setTable(null);

        JsonEventCodec codec = new JsonEventCodec();
        byte[] data = null;
        try {
            data = codec.encode(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);

            RandomAccessFile file = new RandomAccessFile(work, "rw");
            file.write(bos.toByteArray());
            bos.close();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ObjectMapper om = new ObjectMapper();

            os.write(0);
            os.write(om.writeValueAsBytes(event));
            os.flush();
            byte[] datas = os.toByteArray();
            os.close();

            assertequalByteArray(datas, localFileBucket.getNext());

        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetNextFromZipFile() throws IOException {
        Sequence seq = localFileBucket.getStartingSequece();
        DdlEvent event = new DdlEvent();
        event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
        event.setDatabase("cat");
        event.setExecuteTime(0);
        event.setSeq(seq.longValue());
        event.setTable(null);

        LocalFileDataBucketManager bucketIndex = new LocalFileDataBucketManager();
        bucketIndex.setMaster(false);
        bucketIndex.setBaseDir(new File(System.getProperty("java.io.tmpdir", "."), "Puma/zip/").getAbsolutePath());
        bucketIndex.setBucketFilePrefix("bucket-");
        bucketIndex.setMaxBucketLengthMB(10000);
        bucketIndex.start();

        JsonEventCodec codec = new JsonEventCodec();
        byte[] data = null;
        try {
            data = codec.encode(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);

            RandomAccessFile file = new RandomAccessFile(work, "rw");
            file.write(bos.toByteArray());
            bos.close();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ObjectMapper om = new ObjectMapper();

            os.write(0);
            os.write(om.writeValueAsBytes(event));
            os.flush();
            byte[] datas = os.toByteArray();
            os.close();

            bucketIndex.copyFromLocal(new File(System.getProperty("java.io.tmpdir", "."), "Puma/").getAbsolutePath(), "20120710/bucket-0");
            assertequalByteArray(datas,
                    new LocalFileDataBucket(zipWork, new Sequence(120710, 0, 0,0), 10000, "ffff", true).getNext());

        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSeek() {
        boolean thrown = false;

        Sequence seq = localFileBucket.getStartingSequece();
        DdlEvent event = new DdlEvent();
        event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
        event.setDatabase("cat");
        event.setExecuteTime(0);
        event.setSeq(seq.longValue());
        event.setTable(null);
        Sequence newSeq = null;

        JsonEventCodec codec = new JsonEventCodec();
        byte[] data = null;
        try {
            data = codec.encode(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            bos.flush();
            RandomAccessFile file = new RandomAccessFile(work, "rw");
            file.write(bos.toByteArray());
            newSeq = seq.addOffset(bos.size());
            bos.reset();

            event.setSeq(newSeq.longValue());
            data = codec.encode(event);
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            bos.flush();
            file.write(bos.toByteArray());

            bos.close();
            file.close();

            try {
                localFileBucket.skip(newSeq.getOffset());

                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    ObjectMapper om = new ObjectMapper();

                    os.write(0);
                    os.write(om.writeValueAsBytes(event));
                    os.flush();
                    byte[] datas = os.toByteArray();
                    os.close();

                    assertequalByteArray(datas, localFileBucket.getNext());

                } catch (StorageClosedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (StorageClosedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // catch an exception
        try {
            localFileBucket.skip(-1);
        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

    }
    
    @Test
    public void testSeekZipFile() throws IOException {

        Sequence seq = localFileBucket.getStartingSequece();
        DdlEvent event = new DdlEvent();
        event.setSql("CREATE TABLE products (proeduct VARCHAR(10))");
        event.setDatabase("cat");
        event.setExecuteTime(0);
        event.setSeq(seq.longValue());
        event.setTable(null);
        Sequence newSeq = null;
        
        LocalFileDataBucketManager bucketIndex = new LocalFileDataBucketManager();
        bucketIndex.setMaster(false);
        bucketIndex.setBaseDir(new File(System.getProperty("java.io.tmpdir", "."), "Puma/zip/").getAbsolutePath());
        bucketIndex.setBucketFilePrefix("bucket-");
        bucketIndex.setMaxBucketLengthMB(10000);
        bucketIndex.start();

        JsonEventCodec codec = new JsonEventCodec();
        byte[] data = null;
        try {
            data = codec.encode(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            bos.flush();
            RandomAccessFile file = new RandomAccessFile(work, "rw");
            file.write(bos.toByteArray());
            newSeq = seq.addOffset(bos.size());
            bos.reset();

            event.setSeq(newSeq.longValue());
            data = codec.encode(event);
            bos.write(ByteArrayUtils.intToByteArray(data.length));
            bos.write(data);
            bos.flush();
            file.write(bos.toByteArray());

            bos.close();
            file.close();

            try {
                bucketIndex.copyFromLocal(new File(System.getProperty("java.io.tmpdir", "."), "Puma/").getAbsolutePath(), "20120710/bucket-0");
                LocalFileDataBucket zipBucket = new LocalFileDataBucket(zipWork, new Sequence(120710, 0, 0,0), 10000, "ffff", true);
                zipBucket.skip(newSeq.getOffset());

                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    ObjectMapper om = new ObjectMapper();

                    os.write(0);
                    os.write(om.writeValueAsBytes(event));
                    os.flush();
                    byte[] datas = os.toByteArray();
                    os.close();

                    assertequalByteArray(datas, zipBucket.getNext());

                } catch (StorageClosedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (StorageClosedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testClose() {
        Sequence sequence = new Sequence(120710, 0, 0,0);
        LocalFileDataBucket bucket = null;
        try {
            bucket = new LocalFileDataBucket(work, sequence, 10, "20120710/bucket-0", false);
            bucket.stop();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean thrown = false;
        try {
            bucket.checkClosed();
        } catch (StorageClosedException e) {
            thrown = true;
        }

        Assert.assertTrue(thrown);
    }

    @Test
    public void testHasRemainingForWrite() {
        boolean flag = false;
        try {
            flag = localFileBucket.hasRemainingForWrite();
        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(flag);

        localFileBucket.setMaxSizeByte(0);
        try {
            if (localFileBucket.hasRemainingForWrite() != false)
                flag = true;
        } catch (StorageClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(flag);
    }

    protected void assertequalByteArray(byte[] expectedValue, byte[] actualValue) {
        Assert.assertEquals(expectedValue.length, actualValue.length);
        for (int i = 0; i < expectedValue.length; i++) {
            Assert.assertEquals(expectedValue[i], actualValue[i]);
        }

    }

    @After
    public void after() throws IOException {
        try {
            localFileBucket.stop();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (work.delete())
            System.out.println("delete file successfully");
        this.work.getParentFile().delete();
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir", "."), "Puma/zip/"));

    }

    @AfterClass
    public static void destroy() {
    }
}
