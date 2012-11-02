package com.dianping.puma.storage;

import java.io.IOException;
import java.util.TreeMap;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.datatype.BinlogInfo;
import com.dianping.puma.core.datatype.BinlogInfoAndSeq;
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

	public TreeMap<BinlogInfo, BinlogInfoAndSeq> getBinlogIndex();

	public void openBinlogIndex(Sequence seq) throws IOException;

	public long TranBinlogIndexToSeq(BinlogInfo binlogInfo) throws IOException;

	public void binlogIndexFileclose() throws IOException;
	
	public void writeBinlogIndex(BinlogInfo binlogInfo) throws IOException;
	
	public void writeBinlogIndexIntoProperty(BinlogInfoAndSeq bpas) throws IOException;
}
