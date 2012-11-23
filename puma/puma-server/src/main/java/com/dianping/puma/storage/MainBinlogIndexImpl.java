package com.dianping.puma.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

public class MainBinlogIndexImpl implements MainBinlogIndex {
	private AtomicReference<TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>> mainBinlogIndex = new AtomicReference<TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>>();
	private String mainBinlogIndexFileName = "mainBinlogIndex";
	private String mainBinlogIndexBasedir = "/data/applogs/puma/";
	private File mainBinlogIndexFile;
	private BinlogInfoAndSeq startBinlogInfoAndSeq;

	public String getMainBinlogIndexBasedir() {
		return mainBinlogIndexBasedir;
	}

	public void setMainBinlogIndexBasedir(String mainBinlogIndexBasedir) {
		this.mainBinlogIndexBasedir = mainBinlogIndexBasedir;
	}

	public String getMainBinlogIndexFileName() {
		return mainBinlogIndexFileName;
	}

	public void setMainBinlogIndexFileName(String mainBinlogIndexFileName) {
		this.mainBinlogIndexFileName = mainBinlogIndexFileName;
	}

	public void openMainBinlogIndex() throws IOException {
		this.mainBinlogIndex.set(new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>());
		this.mainBinlogIndexFile = new File(mainBinlogIndexBasedir, mainBinlogIndexFileName);
		File mainbinlogIndexBasedir = new File(mainBinlogIndexBasedir);
		if (!mainbinlogIndexBasedir.exists()) {
			if (!mainbinlogIndexBasedir.mkdirs()) {
				throw new IOException(String.format("Can`t creat mainbinlogindexBasedir(%s)!", mainbinlogIndexBasedir
						.getAbsolutePath()));
			}
		}
		this.mainBinlogIndexFile.createNewFile();
	}

	private void updateStartBinlogInfoIndex(BinlogInfoAndSeq begin, BinlogInfoAndSeq end) {
		TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> newBinlogInfoIndexes = new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>(
				mainBinlogIndex.get());
		newBinlogInfoIndexes.put(begin, end);
		mainBinlogIndex.set(newBinlogInfoIndexes);
		this.startBinlogInfoAndSeq = begin;
	}

	public void updateMainBinlogIndex(BinlogInfoAndSeq begin, BinlogInfoAndSeq end) throws IOException {
		if (mainBinlogIndex.get().get(begin) == null) {
			flushMainBinlogIndex();
			updateStartBinlogInfoIndex(begin, end);
		} else {
			BinlogInfoAndSeq temp = mainBinlogIndex.get().get(begin);
			temp.setServerId(end.getServerId());
			temp.setBinlogFile(end.getBinlogFile());
			temp.setBinlogPosition(end.getBinlogPosition());
		}
	}

	public boolean inMainBinlogIndex(long seq) {
		Set<BinlogInfoAndSeq> keys = this.mainBinlogIndex.get().keySet();
		for (BinlogInfoAndSeq key : keys) {
			BinlogInfoAndSeq value = (BinlogInfoAndSeq) this.mainBinlogIndex.get().get(key);
			if (value.getSeq() == seq)
				return true;
		}
		return false;
	}

	private boolean binlogContains(BinlogInfoAndSeq value, BinlogInfoAndSeq start, BinlogInfoAndSeq end) {
		if (value.getServerId() != start.getServerId()) {
			return false;
		}
		if (value.getBinlogFile().compareTo(start.getBinlogFile()) < 0 || value.getBinlogFile().compareTo(end.getBinlogFile()) > 0) {
			return false;
		}
		if (value.getBinlogFile().compareTo(end.getBinlogFile()) == 0 && value.getBinlogPosition() > end.getBinlogPosition()) {
			return false;
		}

		if (value.getBinlogFile().compareTo(start.getBinlogFile()) == 0 && value.getBinlogPosition() < end.getBinlogPosition()) {
			return false;
		}
		return true;
	}

	public void flushMainBinlogIndex() throws IOException {
		if (this.startBinlogInfoAndSeq == null) {
			return;
		}
		BinlogInfoAndSeq key = this.startBinlogInfoAndSeq;
		key.setSeq(-1);
		BinlogInfoAndSeq value = mainBinlogIndex.get().get(key);
		OutputStream mbindex = new FileOutputStream(this.mainBinlogIndexFile, true);
		Properties mbindexitem = new Properties();
		mbindexitem.put(key.toString(), value.toString());
		mbindexitem.store(mbindex, "write a mainbinglogitem");
		mbindex.close();
	}

	public void deleteMainBinlogIndex(String path) {
		Sequence temp = Sequence.convertToSequence(path);
		long deleteitem = temp.longValue();
		TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> map = new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>(this.mainBinlogIndex.get());
		Set<BinlogInfoAndSeq> keys = map.keySet();
		for (BinlogInfoAndSeq key : keys) {
			BinlogInfoAndSeq value = map.get(key);
			if (value.getSeq() == deleteitem) {
				map.remove(key);
				break;
			}
		}
		mainBinlogIndex.set(map);
		try {
			if (this.mainBinlogIndexFile.length() > 1024 * 1024 * 200) {
				File newmbindex = new File(this.mainBinlogIndexBasedir, this.mainBinlogIndexFileName + "_bak");
				OutputStream mbindex = new FileOutputStream(newmbindex);
				Properties bak = new Properties();
				Set<BinlogInfoAndSeq> allkeys = this.mainBinlogIndex.get().keySet();
				for (BinlogInfoAndSeq key : allkeys) {
					BinlogInfoAndSeq value = (BinlogInfoAndSeq) this.mainBinlogIndex.get().get(key);
					bak.put(key.toString(), value.toString());
				}
				bak.store(mbindex, "delete the discard index");
				// TODO delete original file
				newmbindex.renameTo(this.mainBinlogIndexFile);
			}
		} catch (IOException e) {
			// ignore
		}
	}

	public void loadMainBinlogIndex(BucketIndex masterIndex, BucketIndex slaveIndex) throws IOException {
		Properties mainIndexes = new Properties();
		InputStream mbfile = null;
		try {
			mbfile = new FileInputStream(this.mainBinlogIndexFile);
			mainIndexes.load(mbfile);
		} finally {
			if (mbfile != null) {
				try {
					mbfile.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq> newMainBinlogIndex = new TreeMap<BinlogInfoAndSeq, BinlogInfoAndSeq>();

		for (String key : mainIndexes.stringPropertyNames()) {
			String value = mainIndexes.getProperty(key);
			if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
				if (!bucketExist(value, slaveIndex, masterIndex)) {
					continue;
				}
				BinlogInfoAndSeq binlogInfoAndSeqKey = BinlogInfoAndSeq.valueOf(key);
				BinlogInfoAndSeq binlogInfoAndSeqValue = BinlogInfoAndSeq.valueOf(value);
				if (binlogInfoAndSeqKey != null && binlogInfoAndSeqValue != null) {
					newMainBinlogIndex.put(binlogInfoAndSeqKey, binlogInfoAndSeqValue);
				} else {
					throw new IllegalStateException(String.format("Main binlog index corrupted(%s).", this.mainBinlogIndexFile
							.getAbsolutePath()));
				}
			} else {
				throw new IllegalStateException(String.format("Main binlog index corrupted(%s).", this.mainBinlogIndexFile
						.getAbsolutePath()));
			}
		}
		this.mainBinlogIndex.set(newMainBinlogIndex);
	}

	private boolean bucketExist(String indexValue, BucketIndex slaveIndex, BucketIndex masterIndex) {
		BinlogInfoAndSeq binlogInfoAndSeq = BinlogInfoAndSeq.valueOf(indexValue);
		Sequence sequence = new Sequence(Long.valueOf(binlogInfoAndSeq.getSeq()).longValue());
		return !(slaveIndex.getIndex().get().get(sequence) == null && masterIndex.getIndex().get().get(sequence) == null);
	}

	public boolean exists(long seq) {
		Collection<BinlogInfoAndSeq> values = this.mainBinlogIndex.get().values();
		for (BinlogInfoAndSeq value : values) {
			if (value.getSeq() == seq)
				return true;
		}
		return false;
	}

	public Sequence lookupBinlogIndex(BinlogInfoAndSeq binlogInfoAndSeq) {
		Map<BinlogInfoAndSeq, BinlogInfoAndSeq> map = this.mainBinlogIndex.get();
		Set<BinlogInfoAndSeq> keys = map.keySet();
		for (BinlogInfoAndSeq key : keys) {
			BinlogInfoAndSeq value = (BinlogInfoAndSeq) map.get(key);
			if (binlogContains(binlogInfoAndSeq, key, value)) {
				Sequence seq = new Sequence(value.getSeq());
				return seq;
			}
		}
		return null;
	}
}
