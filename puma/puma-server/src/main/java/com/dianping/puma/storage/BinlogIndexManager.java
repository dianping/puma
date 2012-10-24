package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.datatype.BinlogPos;
import com.dianping.puma.core.datatype.BinlogPosAndSeq;
import com.dianping.puma.storage.exception.StorageClosedException;

public class BinlogIndexManager {
	private AtomicReference<TreeMap<BinlogPos, BinlogPosAndSeq>> binlogIndex = new AtomicReference<TreeMap<BinlogPos, BinlogPosAndSeq>>();
	private String mainbinlogIndexFileName = "binlogIndex";
	private String mainbinlogIndexFileNameBasedir = "/data/applogs/puma/";
	private RandomAccessFile mainBinlogIndexFile;
	protected static final String PATH_SEPARATOR = "/";
	private String binlogIndexBaseDir = "/data/applogs/puma/binlogindex";
	private String binlogIndexPrefix = "index-";
	private String bucketFilePrefix = "b-";
	private RandomAccessFile binlogindexfile;

	public BinlogIndexManager(String binlogIndexPrefix) {
		super();
		this.binlogIndexPrefix = binlogIndexPrefix;
	}

	public TreeMap<BinlogPos, BinlogPosAndSeq> getBinlogIndex() {
		return binlogIndex.get();
	}

	public void setBinlogIndex(TreeMap<BinlogPos, BinlogPosAndSeq> binlogIndex) {
		this.binlogIndex.set(binlogIndex);
	}

	public void start() throws IOException {
		this.binlogIndex.set(new TreeMap<BinlogPos, BinlogPosAndSeq>(
				new PathBinlogPosComparator()));
		File mainbindex = new File(mainbinlogIndexFileNameBasedir,
				mainbinlogIndexFileName);
		this.mainBinlogIndexFile = new RandomAccessFile(mainbindex, "rw");
	}

	private void updateStartBinlogPosIndex(Bucket bucket) {
		TreeMap<BinlogPos, BinlogPosAndSeq> newBinlogPosIndexes = new TreeMap<BinlogPos, BinlogPosAndSeq>(
				binlogIndex.get());
		newBinlogPosIndexes.put(bucket.getStartingBinlogPos(),
				new BinlogPosAndSeq(bucket.getCurrentWritingBinlogPos(), bucket
						.getStartingSequece().longValue()));
		binlogIndex.set(newBinlogPosIndexes);
	}

	public void updateFileBinlogIndex(Bucket bucket) {
		if (binlogIndex.get().get(bucket.getStartingBinlogPos()) == null) {
			updateStartBinlogPosIndex(bucket);
		} else {
			binlogIndex.get().get(bucket.getStartingBinlogPos()).setBinlogpos(
					bucket.getCurrentWritingBinlogPos());
		}
	}

	protected String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR
				+ binlogIndexPrefix + seq.getNumber();
	}

	public void openBinlogIndex(Sequence seq) throws IOException {
		File bindex = new File(binlogIndexBaseDir, convertToPath(seq));
		if (!bindex.getParentFile().exists()) {
			if (!bindex.getParentFile().mkdirs()) {
				throw new IOException(String.format(
						"Can't create writeBucket's parent(%s)!", bindex
								.getParent()));
			}
		}

		this.binlogindexfile = new RandomAccessFile(bindex, "rw");
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
	}

	protected static class PathBinlogPosComparator implements
			Comparator<BinlogPos>, Serializable {

		private static final long serialVersionUID = -350477869152651536L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(BinlogPos o1, BinlogPos o2) {
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

	private Boolean binlogContain(BinlogPos value, BinlogPos start,
			BinlogPos end) {
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

	public Boolean getReadBinlogIndex(BinlogPos binlogpos)
			throws StorageClosedException, IOException {
		Map map = this.binlogIndex.get();
		Iterator iter = map.entrySet().iterator();
		Bucket bucket = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			BinlogPos begin = (BinlogPos) entry.getKey();
			BinlogPosAndSeq value = (BinlogPosAndSeq) entry.getValue();
			BinlogPos end = value.getBinlogpos();
			if (binlogContain(binlogpos, begin, end)) {
				Sequence seq = new Sequence(value.getSeq());
				openBinlogIndex(seq);
				return true;
			}
		}
		return false;
	}

	public void writeMainBinlogIndex(byte[] data) throws IOException {
		mainBinlogIndexFile.write(data.length);
		mainBinlogIndexFile.write(data);
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
			BinlogPos begin = (BinlogPos) entry.getKey();
			BinlogPosAndSeq value = (BinlogPosAndSeq) entry.getValue();
			if (value.getSeq() == deleteitem) {
				iter.remove();
				break;
			}
		}
		binlogIndex.set((TreeMap<BinlogPos, BinlogPosAndSeq>) map);
	}

	protected Sequence convertToSequence(String path) {
		String[] parts = path.split(PATH_SEPARATOR);
		return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer
				.valueOf(parts[1].substring(bucketFilePrefix.length())));
	}
}
