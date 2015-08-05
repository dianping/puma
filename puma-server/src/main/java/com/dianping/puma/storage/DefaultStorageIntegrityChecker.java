package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.storage.bucket.DataBucket;
import com.dianping.puma.storage.index.IndexBucket;
import com.dianping.puma.storage.index.IndexKeyImpl;
import com.dianping.puma.storage.index.IndexValueImpl;

/**
 * 为了防止机器突然断电引起的数据文件损坏的异常，故实现对最新的index文件和数据文件进行校验的逻辑。 </br>
 * 一旦某条索引损坏，删除该索引之后的所有索引，数据文件不变； </br>
 * 一旦某条数据损害，删除对应索引之后的所有索引，数据文件不变。</br>
 * 
 * @author damonzhu
 *
 */
public class DefaultStorageIntegrityChecker implements StorageIntegrityChecker {

	private EventStorage eventStorage;

	public DefaultStorageIntegrityChecker(EventStorage eventStorage) {
		this.eventStorage = eventStorage;
	}

	@Override
	public void checkAndRepair() {
		TreeMap<IndexKeyImpl, String> l1Index = eventStorage.getIndexManager().getL1Index();
		EventCodec eventCodec = eventStorage.getEventCodec();

		if (l1Index != null) {
			Entry<IndexKeyImpl, String> lastEntry = l1Index.lastEntry();

			String fileName = lastEntry.getValue();
			IndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = null;
			DataBucket readBucket = null;

			try {
				indexBucket = eventStorage.getIndexManager().getIndexBucket(fileName);
				indexBucket.start();
				IndexValueImpl indexValue = null;
				Sequence lastReadSequence = null;

				while (true) {
					try {
						indexValue = indexBucket.next();
					} catch (EOFException eof) {
						break;
					} catch (IOException e) {
						// handle broken index
						indexBucket.truncate();
						break;
					}

					try {
						if (lastReadSequence == null) {
							lastReadSequence = indexValue.getSequence();
							readBucket = eventStorage.getBucketManager().getReadBucket(lastReadSequence.longValue(), false);
							readBucket.start();
						}

						if (indexValue.getSequence().getOffset() != lastReadSequence.getOffset()) {
							int offset = indexValue.getSequence().getOffset() - lastReadSequence.getOffset()
							      - lastReadSequence.getLen();
							readBucket.skip(offset);
						}

						eventCodec.decode(readBucket.getNext());
						lastReadSequence = indexValue.getSequence();
					} catch (IOException e) {
						// handle broken data, only repair the index
						indexBucket.truncate();
						break;
					}
				}
			} catch (IOException e) {
			} finally {
				if (indexBucket != null) {
					try {
						indexBucket.stop();
					} catch (IOException e) {
					}
				}

				if (readBucket != null) {
					try {
						readBucket.stop();
					} catch (IOException e) {
					}
				}
			}
		}
	}
}
