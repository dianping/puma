package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;

public class DefaultBinlogIndexManager implements BinlogIndexManager {
	private EventCodec codec;
	private volatile boolean stopped = true;
	private BucketIndex masterIndex;
	private BucketIndex slaveIndex;
	private MainBinlogIndex mainBinlogIndex;
	private SubBinlogIndex subBinlogIndex;

	public MainBinlogIndex getMainBinlogIndex() {
		return mainBinlogIndex;
	}

	public void setMainBinlogIndex(MainBinlogIndex mainBinlogIndex) {
		this.mainBinlogIndex = mainBinlogIndex;
	}

	public SubBinlogIndex getSubBinlogIndex() {
		return subBinlogIndex;
	}

	public void setSubBinlogIndex(SubBinlogIndex subBinlogIndex) {
		this.subBinlogIndex = subBinlogIndex;
	}

	/**
	 * @param masterIndex
	 *            the masterIndex to set
	 */
	public void setMasterIndex(BucketIndex masterIndex) {
		this.masterIndex = masterIndex;
	}

	/**
	 * @param slaveIndex
	 *            the slaveIndex to set
	 */
	public void setSlaveIndex(BucketIndex slaveIndex) {
		this.slaveIndex = slaveIndex;
	}

	public EventCodec getCodec() {
		return codec;
	}

	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	@Override
	public void start() throws IOException {
		this.mainBinlogIndex.openMainBinlogIndex();
		this.mainBinlogIndex.loadMainBinlogIndex(masterIndex, slaveIndex);
		addIndexIfNeeded(masterIndex);
		addIndexIfNeeded(slaveIndex);
		stopped = false;
	}

	private void addIndexIfNeeded(BucketIndex bucketIndex) throws IOException {
		for (Sequence seq : bucketIndex.getIndex().get().keySet()) {
			if (!mainIndexExists(seq.longValue())) {
				addBuctetToIndex(seq.longValue(), bucketIndex);
			}
		}
	}

	private boolean mainIndexExists(long seq) {
		return this.mainBinlogIndex.exists(seq);
	}

	private void addBuctetToIndex(long startingSeq, BucketIndex index) throws IOException {
		Bucket bucket = index.getReadBucket(startingSeq, true);
		if (bucket == null) {
			throw new IllegalAccessError(String.format("File not found for sequence(%d)", startingSeq));
		}
		ChangedEvent event = null;
		BinlogInfoAndSeq beginbinlogInfoAndSeq = null;
		BinlogInfoAndSeq endbinlogInfoAndSeq = null;
		String binlogfile = null;
		long binlogpos = -1;
		while (true) {
			try {
				byte[] data = bucket.getNext();
				event = (ChangedEvent) codec.decode(data);
				if (beginbinlogInfoAndSeq == null) {
					beginbinlogInfoAndSeq = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
					beginbinlogInfoAndSeq.setSeq(-1);
					endbinlogInfoAndSeq = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
				}
				if (!StringUtils.equals(binlogfile, event.getBinlog()) || binlogpos != event.getBinlogPos()) {
					BinlogInfoAndSeq newItem = BinlogInfoAndSeq.getBinlogInfoAndSeq(event);
					this.subBinlogIndex.updateSubBinlogIndex(newItem);
					newItem.setSeq(-1);
					binlogfile = event.getBinlog();
					binlogpos = event.getBinlogPos();
				}
			} catch (EOFException e) {
				break;
			}
		}
		if (event == null) {
			return;
		}
		endbinlogInfoAndSeq.setBinlogInfo(event);
		this.mainBinlogIndex.updateMainBinlogIndex(beginbinlogInfoAndSeq, endbinlogInfoAndSeq);
		// TODO merge into flushIndex
	}

	public long tranBinlogIndexToSeq(BinlogInfoAndSeq binlogInfoAndSeq) throws StorageClosedException, IOException {
		Sequence seq = this.mainBinlogIndex.lookupBinlogIndex(binlogInfoAndSeq);
		return this.subBinlogIndex.lookupSubBinlogIndex(seq, binlogInfoAndSeq);
	}

	private void flushBinlogIndex() throws IOException {
		this.mainBinlogIndex.flushMainBinlogIndex();
		this.subBinlogIndex.flushSubBinlogIndex();
	}

	// TODO is there any problem?
	public void deleteBinlogIndex(String path) {
		// TODO refactor all convertToSequence & convertToPath into Sequence
		Sequence temp = Sequence.convertToSequence(path);
		this.subBinlogIndex.deleteSubBinlogIndex(temp);

		// TODO generic
		// TODO copy on write, use tmp hashmap
		this.mainBinlogIndex.deleteMainBinlogIndex(path);
	}

	public void stop() throws IOException {
		if (stopped) {
			try {
				flushBinlogIndex();
			} catch (IOException e) {
				// ignore
			}
			return;
		}

		stopped = true;
	}

	public void updateBinlogIndex(Bucket writingBucket, BinlogInfoAndSeq bpas) throws IOException {
		this.mainBinlogIndex.updateMainBinlogIndex(writingBucket.getStartingBinlogInfoAndSeq(), writingBucket
				.getCurrentWritingBinlogInfoAndSeq());
		this.subBinlogIndex.updateSubBinlogIndex(bpas);
	}
}
