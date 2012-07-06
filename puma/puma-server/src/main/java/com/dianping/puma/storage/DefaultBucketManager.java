package com.dianping.puma.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.puma.core.datatype.Pair;

public class DefaultBucketManager implements BucketManager {
	private File				baseDir;

	private String				name;
	private static final String	BUCKETFILE_PREFIX	= "b";

	public DefaultBucketManager(File baseDir, String name) {
		this.baseDir = baseDir;
		this.name = name;
	}

	@Override
	public Bucket getBucket(long seq) throws IOException {
		String path = getBucketPath(Sequence.valueOf(seq));
		File file = new File(baseDir, path);

		if (file.isFile()) {
			return new FileBucket(file);
		} else {
			// TODO check HDFS bucket
			throw new FileNotFoundException(String.format("Bucket(%s) not found!", file.getCanonicalFile()));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#getNextBucket(long)
	 */
	@Override
	public Pair<Bucket, Long> getNextBucket(long seq) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getBucketPath(Sequence seq) {
		// sample: MainDB/20120705/b001
		return String.format("%s/20%06d/%s%03d", name, seq.group, BUCKETFILE_PREFIX, seq.fileNo);
	}

	private static class Sequence {
		private String	group;
		private int		fileNo;
		private int		offset;

		public Sequence(String group, int fileNo, int offset) {
			this.group = group;
			this.fileNo = fileNo;
			this.offset = offset;
		}

		public static Sequence valueOf(long seq) {
			return new Sequence(String.valueOf(seq >>> 46), ((int) (seq >>> 32)) & 0x00003FFF, (int) seq);
		}

		public long longValue() {
			return Long.valueOf(group) << 46 | ((long) (fileNo & 0x3FFF)) << 32 | offset;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Sequence [group=" + group + ", fileNo=" + fileNo + ", offset=" + offset + "]";
		}

	}

}
