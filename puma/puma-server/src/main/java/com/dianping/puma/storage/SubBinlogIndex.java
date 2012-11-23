package com.dianping.puma.storage;

import java.io.IOException;

public interface SubBinlogIndex {

	public void updateSubBinlogIndex(BinlogInfoAndSeq bpas) throws IOException;
	
	public void flushSubBinlogIndex() throws IOException;
	
	public long lookupSubBinlogIndex(Sequence seq, BinlogInfoAndSeq binlogInfoAndSeq) throws IOException;
	
	public void deleteSubBinlogIndex(Sequence seq);
	
	public void openSubBinlogIndex(Sequence seq) throws IOException;
}
