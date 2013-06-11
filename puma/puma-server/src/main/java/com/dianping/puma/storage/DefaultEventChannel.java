package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageReadException;

public class DefaultEventChannel implements EventChannel {
	private static final Logger logger = Logger
			.getLogger(DefaultEventChannel.class);

	
    private BucketManager    bucketManager;
    private Bucket           bucket;
    private long             seq;
    private volatile boolean stopped = true;
    
    private DecoderableQueue decoderableQueue;

    public DefaultEventChannel(BucketManager bucketManager, long seq, EventCodec codec, boolean fromNext)
            throws StorageException {
        this.bucketManager = bucketManager;
        try {
            bucket = bucketManager.getReadBucket(seq, fromNext);
        } catch (IOException e) {
            throw new InvalidSequenceException("Invalid sequence(" + seq + ")", e);
        }
        this.seq = bucket.getStartingSequece().longValue();
        stopped = false;

        decoderableQueue = new DecoderableQueueImpl(codec);

        Fetcher fetcher = new Fetcher();
    	fetcher.start();

    }

    @Override
    public ChangedEvent next() throws StorageException {
        checkClosed();

        ChangedEvent event = null;

        while (event == null) {
            try {
                checkClosed();

                DecoderElement ele = decoderableQueue.take(500);

                if(ele != null){
                	event = ele.getChangedEvent();
                }

            } catch (EOFException e) {
                try {
                    if (bucketManager.hasNexReadBucket(seq)) {
                        bucket.stop();
                        bucket = bucketManager.getNextReadBucket(seq);
                        seq = bucket.getStartingSequece().longValue();
                    } else {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (IOException ex) {
                    throw new StorageReadException("Failed to read", ex);
                }
            } catch (IOException e) {
                throw new StorageReadException("Failed to read", e);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
			}
        }

        seq = event.getSeq();

        return event;
    }

    private void checkClosed() throws StorageClosedException {
        if (stopped) {
            throw new StorageClosedException("Channel has been closed.");
        }
    }

    @Override
    public void close() {
        if (!stopped) {
            stopped = true;
            if (bucket != null) {
                try {
                    bucket.stop();
                    bucket = null;
                } catch (IOException e) {
                    // ignore
                }
            }
            if( decoderableQueue != null){
                decoderableQueue.close();
            }
        }
    }

    private class Fetcher extends Thread{

		@Override
		public void run() {
			try {
				while(true){
		            checkClosed();
		            //获取一条event的数据之后，放到queue中，并且将入queue
					byte[] data = bucket.getNext();

					DecoderElement element = new DecoderElement();
		            element.setData(data);

		            decoderableQueue.put(element);
				}
			} catch (StorageClosedException e) {
				logger.error("Error when fetching data from bucket, so close storage.", e);
				close();
			} catch (IOException e) {
				logger.error("Error when fetching data from bucket, so close storage.", e);
				close();
			} catch (InterruptedException e) {
				logger.error("Interrupted when fetching data from bucket, so close storage.", e);
				close();
			}
		}
    	
    }
    
}
