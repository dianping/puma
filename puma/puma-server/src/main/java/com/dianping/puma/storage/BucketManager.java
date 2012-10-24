package com.dianping.puma.storage;

import java.io.IOException;
import java.util.TreeMap;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.datatype.BinlogPos;
import com.dianping.puma.core.datatype.BinlogPosAndSeq;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public interface BucketManager extends LifeCycle<StorageLifeCycleException> {
	public Bucket getReadBucket(long seq) throws StorageClosedException,
			IOException;

	public Bucket getNextReadBucket(long seq) throws StorageClosedException,
			IOException;

	public Bucket getNextWriteBucket() throws StorageClosedException,
			IOException;

	public boolean hasNexReadBucket(long seq) throws StorageClosedException,
			IOException;

	public void updateLatestSequence(Sequence sequence);

	public void updateFileBinlogIndex(Bucket bucket);

	public TreeMap<BinlogPos, BinlogPosAndSeq> getBinlogIndex();

	public void openBinlogIndex(Sequence seq) throws IOException;

	public void writeBinlogToIndex(byte[] data) throws IOException;

	public byte[] readBinlogFromIndex() throws IOException;

	public void binlogIndexFileclose() throws IOException;

	public Boolean getReadBinlogIndex(BinlogPos binlogpos)
			throws StorageClosedException, IOException;
}
