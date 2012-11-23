package com.dianping.puma.storage;

import java.io.IOException;

public interface MainBinlogIndex {

	public void openMainBinlogIndex() throws IOException;
	
	public void loadMainBinlogIndex(BucketIndex masterIndex, BucketIndex slaveIndex) throws IOException;
	
	public boolean exists(long seq);
	
	public void updateMainBinlogIndex(BinlogInfoAndSeq begin, BinlogInfoAndSeq end) throws IOException;
	
	public void flushMainBinlogIndex() throws IOException;
	
	public Sequence lookupBinlogIndex(BinlogInfoAndSeq binlogInfoAndSeq);
	
	public void deleteMainBinlogIndex(String path);
}
