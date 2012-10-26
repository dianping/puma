package com.dianping.puma.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.datatype.BinlogInfo;
import com.dianping.puma.core.datatype.BinlogInfoAndSeq;
import com.dianping.puma.storage.exception.StorageClosedException;

public class BinlogIndexManager {
	private AtomicReference<TreeMap<BinlogInfo, BinlogInfoAndSeq>> binlogIndex = new AtomicReference<TreeMap<BinlogInfo, BinlogInfoAndSeq>>();
	private String mainbinlogIndexFileName = "binlogIndex";
	private String mainbinlogIndexFileNameBasedir = "/data/applogs/puma/";
	protected static final String PATH_SEPARATOR = "/";
	private String binlogIndexBaseDir = "/data/applogs/puma/binlogindex";
	private String binlogIndexPrefix = "index-";
	private String bucketFilePrefix = "b-";
	private RandomAccessFile binlogindexfile;
	private String BINLOGINFO_SEPARATOR = "$";
	private File binlogFile;
	private File mainBinlogIndexFile;
	private Properties prop;

	public BinlogIndexManager(String bucketFilePrefix) {
		super();
		this.bucketFilePrefix = bucketFilePrefix;
	}

	public TreeMap<BinlogInfo, BinlogInfoAndSeq> getBinlogIndex() {
		return binlogIndex.get();
	}

	public void setBinlogIndex(TreeMap<BinlogInfo, BinlogInfoAndSeq> binlogIndex) {
		this.binlogIndex.set(binlogIndex);
	}

	public void start() throws IOException {
		this.binlogIndex.set(new TreeMap<BinlogInfo, BinlogInfoAndSeq>(
				new PathBinlogInfoComparator()));
		this.mainBinlogIndexFile = new File(mainbinlogIndexFileNameBasedir,
				mainbinlogIndexFileName);
		this.prop = new Properties();
	}

	private void updateStartBinlogInfoIndex(Bucket bucket) {
		TreeMap<BinlogInfo, BinlogInfoAndSeq> newBinlogInfoIndexes = new TreeMap<BinlogInfo, BinlogInfoAndSeq>(
				binlogIndex.get());
		newBinlogInfoIndexes.put(bucket.getStartingBinlogInfo(),
				new BinlogInfoAndSeq(bucket.getCurrentWritingBinlogInfo(),
						bucket.getStartingSequece().longValue()));
		binlogIndex.set(newBinlogInfoIndexes);
	}

	public void updateFileBinlogIndex(Bucket bucket) {
		if (binlogIndex.get().get(bucket.getStartingBinlogInfo()) == null) {
			updateStartBinlogInfoIndex(bucket);
		} else {
			binlogIndex.get().get(bucket.getStartingBinlogInfo())
					.setBinlogInfo(bucket.getCurrentWritingBinlogInfo());
		}
	}

	protected String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR
				+ binlogIndexPrefix + seq.getNumber();
	}

	public void openBinlogIndex(Sequence seq) throws IOException {
		this.binlogFile = new File(binlogIndexBaseDir, convertToPath(seq));
		if (!this.binlogFile.getParentFile().exists()) {
			if (!this.binlogFile.getParentFile().mkdirs()) {
				throw new IOException(String.format(
						"Can't create writeBucket's parent(%s)!",
						this.binlogFile.getParent()));
			}
		}

		this.binlogindexfile = new RandomAccessFile(this.binlogFile, "rw");
	}

	public void writeBinlogToIndex(byte[] data) throws IOException {
		this.binlogindexfile.write(data);
	}

	public byte[] readBinlogFromIndex() throws IOException {
		int length = this.binlogindexfile.readInt();
		byte[] data = new byte[length];
		int n = 0;
		while (n < length) {
			int count = this.binlogindexfile.read(data, 0 + n, length - n);
			n += count;
		}
		return data;
	}

	public void binlogIndexFileclose() throws IOException {
		this.binlogindexfile.close();
		this.binlogindexfile = null;
		this.prop.clear();
	}

	protected static class PathBinlogInfoComparator implements
			Comparator<BinlogInfo>, Serializable {

		private static final long serialVersionUID = -350477869152651536L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(BinlogInfo o1, BinlogInfo o2) {
			if (o1.getServerId() < o2.getServerId()) {
				return -1;
			} else if (o1.getServerId() == o2.getServerId()) {
				if (o1.getBinlogFile().compareTo(o2.getBinlogFile()) < 0) {
					return -1;
				} else if (o1.getBinlogFile().compareTo(o2.getBinlogFile()) == 0) {
					if (o1.getBinlogPosition() < o2.getBinlogPosition()) {
						return -1;
					} else if (o1.getBinlogPosition() == o2.getBinlogPosition()) {
						return 0;
					} else {
						return 1;
					}
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		}

	}

	private Boolean binlogContain(BinlogInfo value, BinlogInfo start,
			BinlogInfo end) {
		if (value.getServerId() != start.getServerId())
			return false;
		if (value.getBinlogFile().compareTo(start.getBinlogFile()) < 0
				|| value.getBinlogFile().compareTo(end.getBinlogFile()) > 0)
			return false;
		if (value.getBinlogFile().compareTo(end.getBinlogFile()) == 0
				&& value.getBinlogPosition() > end.getBinlogPosition())
			return false;
		return true;
	}

	public Boolean getReadBinlogIndex(BinlogInfo BinlogInfo)
			throws StorageClosedException, IOException {
		Map map = this.binlogIndex.get();
		Iterator iter = map.entrySet().iterator();
		Bucket bucket = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			BinlogInfo begin = (BinlogInfo) entry.getKey();
			BinlogInfoAndSeq value = (BinlogInfoAndSeq) entry.getValue();
			BinlogInfo end = value.getBinlogInfo();
			if (binlogContain(BinlogInfo, begin, end)) {
				Sequence seq = new Sequence(value.getSeq());
				openBinlogIndex(seq);
				return true;
			}
		}
		return false;
	}

	public void writeMainBinlogIndex(BinlogInfo binlogInfo) throws IOException {
		BinlogInfoAndSeq value = binlogIndex.get().get(binlogInfo);
		try {
			OutputStream binlogindex = new FileOutputStream(this.binlogFile);
			prop.store(binlogindex, "write to binlogfile");
			binlogindex.close();
			OutputStream mainbinlogindex = new FileOutputStream(this.mainBinlogIndexFile, true);
			Properties mainindexitem = new Properties();
			mainindexitem.put(convertBinlogInfoToString(binlogInfo),convertBinlogInfoAndSeqToString(value));
			mainindexitem.store(mainbinlogindex, "write a mainbinglogitem");
			mainbinlogindex.close();
		} catch (IOException e) {
			System.err.println("write to binlogfile fail!");
		}
	}

	public void deleteBinlogIndexFile(Sequence seq) {
		File bindex = new File(binlogIndexBaseDir, convertToPath(seq));
		bindex.delete();
	}

	// TODO is there any problem?
	public void deleteBinlogIndex(String path) {
		long deleteitem = convertToSequence(path).longValue();
		deleteBinlogIndexFile(convertToSequence(path));
		Map map = this.binlogIndex.get();
		Iterator iter = map.entrySet().iterator();
		Bucket bucket = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			BinlogInfo begin = (BinlogInfo) entry.getKey();
			BinlogInfoAndSeq value = (BinlogInfoAndSeq) entry.getValue();
			if (value.getSeq() == deleteitem) {
				iter.remove();
				break;
			}
		}
		binlogIndex.set((TreeMap<BinlogInfo, BinlogInfoAndSeq>) map);
	}

	protected Sequence convertToSequence(String path) {
		String[] parts = path.split(PATH_SEPARATOR);
		return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer
				.valueOf(parts[1].substring(bucketFilePrefix.length())));
	}

	public void writeBinlogIndexIntoProperty(BinlogInfoAndSeq bpas)
			throws IOException {
		prop.put(convertBinlogInfoToString(bpas.getBinlogInfo()), String
				.valueOf(bpas.getSeq()));
	}

	public String convertBinlogInfoToString(BinlogInfo binloginfo) {
		return String.valueOf(binloginfo.getServerId()) + BINLOGINFO_SEPARATOR
				+ binloginfo.getBinlogFile() + BINLOGINFO_SEPARATOR
				+ String.valueOf(binloginfo.getBinlogPosition());
	}

	public String convertBinlogInfoAndSeqToString(
			BinlogInfoAndSeq binlogInfoAndSeq) {
		return String.valueOf(binlogInfoAndSeq.getBinlogInfo().getServerId())
				+ this.BINLOGINFO_SEPARATOR
				+ binlogInfoAndSeq.getBinlogInfo().getBinlogFile()
				+ this.BINLOGINFO_SEPARATOR
				+ String.valueOf(binlogInfoAndSeq.getBinlogInfo()
						.getBinlogPosition()) + this.BINLOGINFO_SEPARATOR
				+ String.valueOf(binlogInfoAndSeq.getSeq());
	}
}
