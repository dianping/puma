package com.dianping.puma.storage.bucket;

import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.storage.StorageBaseTest;
import com.google.common.base.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;

public class LengthReadBucketTest extends StorageBaseTest {

    LengthReadBucket lengthReadBucket;

    File bucket;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        bucket = new File(testDir, "bucket");
        createFile(bucket);

        lengthReadBucket = BucketFactory.newLengthReadBucket(bucket);
        lengthReadBucket.start();
    }

    @Test
    public void testName() throws Exception {
        lengthReadBucket = BucketFactory.newLengthReadBucket(new File("/Users/Dozer/Desktop/dozer-bucket-1.data"));
        lengthReadBucket.start();

        RawEventCodec codec = new RawEventCodec();

        int all = 0;
        int onlyBuy = 0;

        while (true) {
            Event item = codec.decode(lengthReadBucket.next());
            if (item instanceof RowChangedEvent) {
                RowChangedEvent rowChangedEvent = (RowChangedEvent) item;

                if (rowChangedEvent.getColumns() != null &&
                        rowChangedEvent.getColumns().containsKey("BuySuccessTime") &&
                        !Objects.equal(rowChangedEvent.getColumns().get("BuySuccessTime").getNewValue(),
                                rowChangedEvent.getColumns().get("BuySuccessTime").getOldValue())) {
                    onlyBuy++;
                    System.out.println(rowChangedEvent);
                }
                all++;

//                if ("UOD_Order431".equals(rowChangedEvent.getTable()) &&
//                        rowChangedEvent.getColumns() != null &&
//                        rowChangedEvent.getColumns().containsKey("OrderID") &&
//                        rowChangedEvent.getColumns().get("OrderID").getNewValue().equals("144794873982286850917198")
//                        ) {
//                    System.out.println(rowChangedEvent.toString());
//                }

                System.out.println(1.0 * onlyBuy / all);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        lengthReadBucket.stop();

        super.tearDown();
    }

    @Test
    public void testNext() throws Exception {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("apple".getBytes());
        os.writeInt("banana".getBytes().length);
        os.write("banana".getBytes());
        os.writeInt("car".getBytes().length);
        os.write("car".getBytes());
        os.writeInt("dog".getBytes().length);
        os.write("dog".getBytes());
        os.flush();

        assertArrayEquals("apple".getBytes(), lengthReadBucket.next());
        assertArrayEquals("banana".getBytes(), lengthReadBucket.next());
        assertArrayEquals("car".getBytes(), lengthReadBucket.next());
        assertArrayEquals("dog".getBytes(), lengthReadBucket.next());
    }

    @Test(expected = EOFException.class)
    public void testNextEOF() throws IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("appl".getBytes());
        os.flush();

        lengthReadBucket.next();
    }

    @Test(expected = IOException.class)
    public void testNextNegativeLength() throws IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt(-1);
        os.write("apple".getBytes());
        os.flush();

        lengthReadBucket.next();
    }

    @Test
    public void testSkip() throws Exception {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(bucket));
        os.writeInt("apple".getBytes().length);
        os.write("apple".getBytes());
        os.writeInt("banana".getBytes().length);
        os.write("banana".getBytes());
        os.writeInt("car".getBytes().length);
        os.write("car".getBytes());
        os.flush();

        assertArrayEquals("apple".getBytes(), lengthReadBucket.next());
        lengthReadBucket.skip(4 + "banana".getBytes().length);
        assertArrayEquals("car".getBytes(), lengthReadBucket.next());
    }

    @Test
    public void testCheckCompressed() throws Exception {

    }
}