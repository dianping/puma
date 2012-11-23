package com.dianping.puma.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SubBinlogIndexImpl implements SubBinlogIndex{
	private String subBinlogIndexBaseDir = "/data/applogs/puma/binlogindex";
	private String subBinlogIndexPrefix = "index-";
	private File subBinlogFile;
	private Properties writingSubIndex = new Properties();

	public String getSubBinlogIndexPrefix() {
		return subBinlogIndexPrefix;
	}

	public void setSubBinlogIndexPrefix(String subBinlogIndexPrefix) {
		this.subBinlogIndexPrefix = subBinlogIndexPrefix;
	}

	public String getSubBinlogIndexBaseDir() {
		return subBinlogIndexBaseDir;
	}

	public void setSubBinlogIndexBaseDir(String subBinlogIndexBaseDir) {
		this.subBinlogIndexBaseDir = subBinlogIndexBaseDir;
	}

	public void flushSubBinlogIndex() throws IOException {
		if(this.subBinlogFile == null){
			return;
		}
		OutputStream sbindex = new FileOutputStream(this.subBinlogFile);
		this.writingSubIndex.store(sbindex, "write to subbinlogfile");
		sbindex.close();
	}

	public void updateSubBinlogIndex(BinlogInfoAndSeq bpas) throws IOException {
		Sequence tmpseq = new Sequence(bpas.getSeq());
		BinlogInfoAndSeq tmpItem = bpas;
		if (tmpseq.getOffset() == 0) {
			flushSubBinlogIndex();
			openSubBinlogIndex(tmpseq);
		}
		tmpItem.setSeq(-1);
		this.writingSubIndex.put(tmpItem.toString(), String.valueOf(tmpseq.longValue()));
	}

	public void deleteSubBinlogIndex(Sequence seq) {
		File bindex = new File(subBinlogIndexBaseDir, convertToSubBinlogIndexPath(seq));
		bindex.delete();
	}

	public void openSubBinlogIndex(Sequence seq) throws IOException {
		this.subBinlogFile = new File(this.subBinlogIndexBaseDir, convertToSubBinlogIndexPath(seq));
		if (!this.subBinlogFile.getParentFile().exists()) {
			if (!this.subBinlogFile.getParentFile().mkdirs()) {
				throw new IOException(String.format("Can't create writeBucket's parent(%s)!", this.subBinlogFile.getParent()));
			}
		}
	}

	public long lookupSubBinlogIndex(Sequence seq, BinlogInfoAndSeq binlogInfoAndSeq) throws IOException {
		Properties result = new Properties();
		File bfile = new File(subBinlogIndexBaseDir, convertToSubBinlogIndexPath(seq));
		if (bfile.exists()) {
			InputStream inStream = new FileInputStream(bfile);
			result.load(inStream);
			inStream.close();
		} else {
			String temp = this.writingSubIndex.getProperty(binlogInfoAndSeq.toString());
			if (temp == null) {
				return -1;
			} else {
				return Long.valueOf(temp).longValue();
			}
		}
		if (result == null)
			return -1;
		String temp = result.getProperty(binlogInfoAndSeq.toString());
		if (temp == null) {
			return -1;
		} else {
			return Long.valueOf(temp).longValue();
		}
	}
	
	public String convertToSubBinlogIndexPath(Sequence seq) {
		return "20" + seq.getCreationDate() + Sequence.PATH_SEPARATOR + subBinlogIndexPrefix + seq.getNumber();
	}
}
