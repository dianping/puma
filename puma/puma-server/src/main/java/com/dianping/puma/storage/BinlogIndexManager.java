package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.datatype.BinlogInfo;
import com.dianping.puma.core.datatype.BinlogInfoAndSeq;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageClosedException;

public class BinlogIndexManager {
	private AtomicReference<TreeMap<BinlogInfo, BinlogInfoAndSeq>> mainBinlogIndex = new AtomicReference<TreeMap<BinlogInfo, BinlogInfoAndSeq>>();
	private String mainbinlogIndexFileName = "binlogIndex";
	private String mainbinlogIndexFileNameBasedir = "/data/applogs/puma/";
	protected static final String PATH_SEPARATOR = "/";
	private String subBinlogIndexBaseDir = "/data/applogs/puma/binlogindex";
	private String subBinlogIndexPrefix = "index-";
	private String bucketFilePrefix = "b-";
	private String BINLOGINFO_SEPARATOR = "$";
	private File subBinlogFile;
	private File mainBinlogIndexFile;
	private Properties prop;
	private EventCodec codec;

	public TreeMap<BinlogInfo, BinlogInfoAndSeq> getBinlogIndex() {
		return mainBinlogIndex.get();
	}

	public void setBinlogIndex(TreeMap<BinlogInfo, BinlogInfoAndSeq> binlogIndex) {
		this.mainBinlogIndex.set(binlogIndex);
	}

	public void start(BucketIndex masterIndex, BucketIndex slaveIndex)
			throws IOException {
		this.mainBinlogIndex.set(new TreeMap<BinlogInfo, BinlogInfoAndSeq>(
				new PathBinlogInfoComparator()));
		this.mainBinlogIndexFile = new File(mainbinlogIndexFileNameBasedir,
				mainbinlogIndexFileName);
		this.prop = new Properties();
		if (!this.mainBinlogIndexFile.exists())
			if (!this.mainBinlogIndexFile.createNewFile())
				throw new IOException("Can`t creat mainbinlogindexfile!");
		InputStream mbfile = new FileInputStream(this.mainBinlogIndexFile);
		Properties properties = new Properties();
		properties.load(mbfile);
		mbfile.close();
		Iterator propIter = properties.entrySet().iterator();
		while (propIter.hasNext()) {
			Map.Entry entry = (Map.Entry) propIter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (!hasCleaned(value, slaveIndex, masterIndex))
				continue;
			try {
				mainBinlogIndex.get().put(convertStringToBinlogInfo(key),
						convertStringToBinlogInfoAndSeq(value));
			} catch (Exception e) {
				// TODO is there any more reasonable method
				continue;
			}
		}
		int lostnum = masterIndex.size() + slaveIndex.size()
				- mainBinlogIndex.get().size();
		// TODO build the complete binlog index
		while (lostnum-- > 0) {
			Bucket bucket = masterIndex.getReadBucket(masterIndex.getIndex()
					.get().lastEntry().getKey().longValue(), true);
			ChangedEvent event = null;
			BinlogInfo binlogInfo = null;
			BinlogInfoAndSeq binlogInfoAndSeq = null;
			String binlogfile = null;
			long binlogpos = 0;
			while (true) {
				try {
					byte[] data = bucket.getNext();
					event = (ChangedEvent) codec.decode(data);
					if (binlogInfo == null) {
						binlogInfo = new BinlogInfo(event.getServerId(), event
								.getBinlog(), event.getBinlogPos());
						binlogInfoAndSeq = new BinlogInfoAndSeq(null, event
								.getSeq());

					}
					if (binlogfile != event.getBinlog()
							|| binlogpos != event.getBinlogPos()) {
						this.prop.put(convertBinlogInfoToString(new BinlogInfo(
								event.getServerId(), event.getBinlog(), event
										.getBinlogPos())), String.valueOf(event
								.getSeq()));
						binlogfile = event.getBinlog();
						binlogpos = event.getBinlogPos();
					}
				} catch (EOFException e) {
					break;
				}
			}
			binlogInfoAndSeq.setBinlogInfo(new BinlogInfo(event.getServerId(),
					event.getBinlog(), event.getBinlogPos()));
			mainBinlogIndex.get().put(binlogInfo, binlogInfoAndSeq);
			openBinlogIndex(new Sequence(binlogInfoAndSeq.getSeq()));
			writeBinlogIndex(binlogInfo);
			closebinlogIndexFile();
		}
	}

	public Boolean hasCleaned(String s, BucketIndex slaveIndex,
			BucketIndex masterIndex) {
		if (slaveIndex.getIndex().get().get(
				new Sequence(Long.valueOf(
						s.substring(s.lastIndexOf(BINLOGINFO_SEPARATOR) + 1))
						.longValue())) == null
				&& masterIndex
						.getIndex()
						.get()
						.get(
								new Sequence(
										Long
												.valueOf(
														s
																.substring(s
																		.lastIndexOf(BINLOGINFO_SEPARATOR) + 1))
												.longValue())) == null) {
			return false;
		} else {
			return true;
		}
	}

	public BinlogInfo convertStringToBinlogInfo(String s) throws Exception {
		int begin = 0;
		int end = 0;
		long serverId;
		String binlogFile;
		long binlogPos;
		end = s.indexOf(BINLOGINFO_SEPARATOR);
		if (end == -1)
			throw new Exception();
		serverId = Long.valueOf(s.substring(begin, end)).longValue();
		begin = end + 1;
		end = s.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1)
			throw new Exception();
		binlogFile = s.substring(begin, end);
		begin = end + 1;
		binlogPos = Long.valueOf(s.substring(begin));
		return new BinlogInfo(serverId, binlogFile, binlogPos);
	}

	public BinlogInfoAndSeq convertStringToBinlogInfoAndSeq(String s)
			throws Exception {
		int begin = 0;
		int end = 0;
		long serverId;
		String binlogFile;
		long binlogPos;
		long seq;
		end = s.indexOf(BINLOGINFO_SEPARATOR);
		if (end == -1)
			throw new Exception();
		serverId = Long.valueOf(s.substring(begin, end)).longValue();
		begin = end + 1;
		end = s.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1)
			throw new Exception();
		binlogFile = s.substring(begin, end);
		begin = end + 1;
		end = s.indexOf(BINLOGINFO_SEPARATOR, begin);
		if (end == -1)
			throw new Exception();
		binlogPos = Long.valueOf(s.substring(begin, end));
		begin = end + 1;
		seq = Long.valueOf(s.indexOf(BINLOGINFO_SEPARATOR));
		return new BinlogInfoAndSeq(serverId, binlogFile, binlogPos, seq);
	}

	private void updateStartBinlogInfoIndex(Bucket bucket) {
		TreeMap<BinlogInfo, BinlogInfoAndSeq> newBinlogInfoIndexes = new TreeMap<BinlogInfo, BinlogInfoAndSeq>(
				mainBinlogIndex.get());
		newBinlogInfoIndexes.put(bucket.getStartingBinlogInfo(),
				new BinlogInfoAndSeq(bucket.getCurrentWritingBinlogInfo(),
						bucket.getStartingSequece().longValue()));
		mainBinlogIndex.set(newBinlogInfoIndexes);
	}

	public void updateFileBinlogIndex(Bucket bucket) {
		if (mainBinlogIndex.get().get(bucket.getStartingBinlogInfo()) == null) {
			updateStartBinlogInfoIndex(bucket);
		} else {
			mainBinlogIndex.get().get(bucket.getStartingBinlogInfo())
					.setBinlogInfo(bucket.getCurrentWritingBinlogInfo());
		}
	}

	protected String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR
				+ this.subBinlogIndexPrefix + seq.getNumber();
	}

	public void openBinlogIndex(Sequence seq) throws IOException {
		this.subBinlogFile = new File(this.subBinlogIndexBaseDir, convertToPath(seq));
		if (!this.subBinlogFile.getParentFile().exists()) {
			if (!this.subBinlogFile.getParentFile().mkdirs()) {
				throw new IOException(String.format(
						"Can't create writeBucket's parent(%s)!",
						this.subBinlogFile.getParent()));
			}
		}
	}

	public long readBinlogIndex(Sequence seq, BinlogInfo binlogInfo)
			throws IOException {
		Properties result = new Properties();
		File bfile = new File(subBinlogIndexBaseDir, convertToPath(seq));
		InputStream inStream = new FileInputStream(bfile);
		inStream.close();
		result.load(inStream);
		String temp = result.getProperty(convertBinlogInfoToString(binlogInfo));
		if (temp == null) {
			return -1;
		} else {
			return Long.valueOf(temp).longValue();
		}
	}

	public void closebinlogIndexFile() throws IOException {
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

	public long TranBinlogIndexToSeq(BinlogInfo binlogInfo)
			throws StorageClosedException, IOException {
		Map map = this.mainBinlogIndex.get();
		Iterator iter = map.entrySet().iterator();
		Bucket bucket = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			BinlogInfo begin = (BinlogInfo) entry.getKey();
			BinlogInfoAndSeq value = (BinlogInfoAndSeq) entry.getValue();
			BinlogInfo end = value.getBinlogInfo();
			if (binlogContain(binlogInfo, begin, end)) {
				Sequence seq = new Sequence(value.getSeq());
				return readBinlogIndex(seq, binlogInfo);
			}
		}
		return -1;
	}

	public void writeBinlogIndex(BinlogInfo binlogInfo) throws IOException {
		BinlogInfoAndSeq value = mainBinlogIndex.get().get(binlogInfo);
		try {
			OutputStream sbindex = new FileOutputStream(this.subBinlogFile);
			this.prop.store(sbindex, "write to binlogfile");
			sbindex.close();
			OutputStream mbindex = new FileOutputStream(
					this.mainBinlogIndexFile, true);
			Properties mbindexitem = new Properties();
			mbindexitem.put(convertBinlogInfoToString(binlogInfo),
					convertBinlogInfoAndSeqToString(value));
			mbindexitem.store(mbindex, "write a mainbinglogitem");
			mbindex.close();
		} catch (IOException e) {
			System.err.println("write to binlogfile fail!");
		}
	}

	public void deleteBinlogIndexFile(Sequence seq) {
		File bindex = new File(subBinlogIndexBaseDir, convertToPath(seq));
		bindex.delete();
	}

	// TODO is there any problem?
	public void deleteBinlogIndex(String path) {
		long deleteitem = convertToSequence(path).longValue();
		deleteBinlogIndexFile(convertToSequence(path));
		Map map = this.mainBinlogIndex.get();
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
		mainBinlogIndex.set((TreeMap<BinlogInfo, BinlogInfoAndSeq>) map);
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

	public String getMainbinlogIndexFileName() {
		return mainbinlogIndexFileName;
	}

	public void setMainbinlogIndexFileName(String mainbinlogIndexFileName) {
		this.mainbinlogIndexFileName = mainbinlogIndexFileName;
	}

	public String getMainbinlogIndexFileNameBasedir() {
		return mainbinlogIndexFileNameBasedir;
	}

	public void setMainbinlogIndexFileNameBasedir(
			String mainbinlogIndexFileNameBasedir) {
		this.mainbinlogIndexFileNameBasedir = mainbinlogIndexFileNameBasedir;
	}

	public String getSubBinlogIndexBaseDir() {
		return subBinlogIndexBaseDir;
	}

	public void setSubBinlogIndexBaseDir(String subBinlogIndexBaseDir) {
		this.subBinlogIndexBaseDir = subBinlogIndexBaseDir;
	}

	public String getSubBinlogIndexPrefix() {
		return subBinlogIndexPrefix;
	}

	public void setSubBinlogIndexPrefix(String subBinlogIndexPrefix) {
		this.subBinlogIndexPrefix = subBinlogIndexPrefix;
	}

	public String getBucketFilePrefix() {
		return bucketFilePrefix;
	}

	public void setBucketFilePrefix(String bucketFilePrefix) {
		this.bucketFilePrefix = bucketFilePrefix;
	}

	public EventCodec getCodec() {
		return codec;
	}

	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}
}
