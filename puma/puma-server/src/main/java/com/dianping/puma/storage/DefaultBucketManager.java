package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;

import com.dianping.puma.core.codec.EventCodec;

public class DefaultBucketManager implements BucketManager {
	private File										localBaseDir;
	private int											localBucketMaxSizeMB;
	private String										bucketFilePrefix;
	private EventCodec									codec;
	private volatile boolean							stopped			= false;

	private Configuration								hdfsConfig;
	private String										hdfsBaseDir;
	private FileSystem									fileSystem;

	private AtomicReference<TreeMap<Sequence, String>>	localBuckets	= new AtomicReference<TreeMap<Sequence, String>>();
	private AtomicReference<TreeMap<Sequence, String>>	hdfsBuckets		= new AtomicReference<TreeMap<Sequence, String>>();

	public void setHdfsBaseDir(String hdfsBaseDir) {
		this.hdfsBaseDir = hdfsBaseDir;
	}

	public void initHdfsConfiguration() {
		hdfsConfig = new Configuration();
		Properties prop = new Properties();
		InputStream propIn = null;

		try {
			propIn = DefaultBucketManager.class.getClassLoader().getResourceAsStream("hdfs.properties");
			prop.load(propIn);

			for (String key : prop.stringPropertyNames()) {
				hdfsConfig.set(key, prop.getProperty(key));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (propIn != null) {
				try {
					propIn.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		UserGroupInformation.setConfiguration(hdfsConfig);
		try {
			SecurityUtil.login(hdfsConfig, prop.getProperty("keytabFileKey"), prop.getProperty("userNameKey"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public DefaultBucketManager(String localBaseDir, String hdfsBaseDir, String name, String bucketFilePrefix,
			int localBucketMaxSizeMB, EventCodec codec) throws IOException {
		this.localBaseDir = new File(localBaseDir, name);
		this.localBucketMaxSizeMB = localBucketMaxSizeMB;
		this.bucketFilePrefix = bucketFilePrefix;
		this.codec = codec;
		this.hdfsBaseDir = hdfsBaseDir;

		initHdfsConfiguration();
		this.fileSystem = FileSystem.get(this.hdfsConfig);

		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void init() throws IOException, URISyntaxException {
		if (!localBaseDir.exists()) {
			localBaseDir.mkdirs();
		}

		initLocalBuckets();
		initHDFSBucket();

	}

	private void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket manager has been closed.");
		}
	}

	@Override
	public Bucket getReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = null;
		String path = null;
		boolean hdfs = false; // flag whether hdfs or local bucket

		if (seq == -1L) {
			// find hdfs first
			if (hdfsBuckets.get().isEmpty() == false) {
				path = hdfsBuckets.get().firstEntry().getValue();
				sequence = new Sequence(hdfsBuckets.get().firstEntry().getKey());

			} else if (localBuckets.get().isEmpty() == false) {
				path = localBuckets.get().firstEntry().getValue();
				sequence = new Sequence(localBuckets.get().firstEntry().getKey());
				hdfs = true;
			} else {
				// TODO invalid seq
				path = null;
			}
		} else {
			sequence = new Sequence(seq);
			path = localBuckets.get().get(sequence);
			if (path == null) {
				path = hdfsBuckets.get().get(sequence);
				if (path != null) {
					hdfs = true;
				}
			}

		}

		if (path != null) {
			int offset = sequence.getOffset();
			Bucket bucket = null;

			if (hdfs == false) {
				File file = new File(localBaseDir, path);
				bucket = new FileBucket(file, sequence.clearOffset(), localBucketMaxSizeMB, codec);

			} else {
				bucket = new HDFSBucket(this.fileSystem, hdfsBaseDir + path, sequence.clearOffset(), codec);
			}

			bucket.seek(offset);
			try {
				bucket.getNext();
			} catch (EOFException e) {
				// ignore
			}

			return bucket;
		} else {
			throw new FileNotFoundException(String.format("Bucket(%d) not found!", path));
		}

	}

	@Override
	public Bucket getNextReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence = sequence.clearOffset();
		NavigableMap<Sequence, String> localTailMap = localBuckets.get().tailMap(sequence, false);
		NavigableMap<Sequence, String> hdfsTailMap = hdfsBuckets.get().tailMap(sequence, false);

		if (localTailMap.isEmpty() != false) {
			Entry<Sequence, String> firstEntry = localTailMap.firstEntry();
			File file = new File(localBaseDir, firstEntry.getValue());
			return new FileBucket(file, firstEntry.getKey(), localBucketMaxSizeMB, codec);
		} else if (hdfsTailMap.isEmpty() != false) {
			Entry<Sequence, String> firstEntry = localTailMap.firstEntry();
			return new HDFSBucket(this.fileSystem, hdfsBaseDir + firstEntry.getValue(), firstEntry.getKey(), codec);
		} else {
			throw new FileNotFoundException("No next read bucket for seq(" + seq + ")");
		}

	}

	@Override
	public Bucket getNextWriteBucket() throws IOException {
		checkClosed();
		Entry<Sequence, String> lastEntry = localBuckets.get().lastEntry();
		Sequence nextSeq = null;
		if (lastEntry == null) {
			nextSeq = new Sequence(getNowCreationDate(), 0);
		} else {
			nextSeq = getNextWriteBucketSequence(new Sequence(lastEntry.getKey()));
		}
		String bucketPath = convertToPath(nextSeq);
		File bucketFile = new File(localBaseDir, bucketPath);

		if (!bucketFile.getParentFile().exists()) {
			if (!bucketFile.getParentFile().mkdirs()) {
				throw new IOException(String.format("Can't create writeBucket's parent(%s)!", bucketFile.getParent()));
			}
		}

		if (!bucketFile.createNewFile()) {
			throw new IOException(String.format("Can't create writeBucket(%s)!", bucketFile.getAbsolutePath()));
		} else {
			// Copy-On-Write, since this case occur rarely
			TreeMap<Sequence, String> tmpLocalBuckets = new TreeMap<Sequence, String>(localBuckets.get());
			tmpLocalBuckets.put(nextSeq, bucketPath);
			localBuckets.set(tmpLocalBuckets);
			return new FileBucket(bucketFile, nextSeq, localBucketMaxSizeMB, codec);
		}
	}

	private int getNowCreationDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		return Integer.valueOf(sdf.format(new Date()));
	}

	private Sequence getNextWriteBucketSequence(Sequence seq) {
		if (getNowCreationDate() == seq.getCreationDate()) {
			return seq.getNext(true);
		} else {
			return seq.getNext(false);
		}
	}

	private String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + File.separator + bucketFilePrefix + seq.getNumber();
	}

	private Sequence convertToSequence(String path) {
		String[] parts = path.split(File.separator);
		return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer.valueOf(parts[1].substring(bucketFilePrefix
				.length())));
	}

	private void initLocalBuckets() {
		localBuckets.set(new TreeMap<Sequence, String>(new PathSequenceComparator()));
		File[] dirs = localBaseDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {
						return true;
					}
				}
				return false;
			};
		});

		if (dirs != null) {
			for (File dir : dirs) {
				String[] subFiles = dir.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if (name.startsWith(bucketFilePrefix)
								&& StringUtils.isNumeric(name.substring(bucketFilePrefix.length()))) {
							return true;
						}
						return false;
					}
				});

				for (String subFile : subFiles) {
					String path = dir.getName() + File.separator + subFile;
					localBuckets.get().put(convertToSequence(path), path);
				}
			}
		}
	}

	private void initHDFSBucket() throws IOException, URISyntaxException {

		hdfsBuckets.set(new TreeMap<Sequence, String>(new PathSequenceComparator()));

		if (this.fileSystem.getFileStatus(new Path(this.hdfsBaseDir)).isDir()) {

			FileStatus[] dirsStatus = this.fileSystem.listStatus(new Path(this.hdfsBaseDir));
			Path[] listedPaths = FileUtil.stat2Paths(dirsStatus);

			for (Path pathname : listedPaths) {

				if (this.fileSystem.getFileStatus(pathname).isDir()) {
					if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {

						FileStatus[] status = this.fileSystem.listStatus(pathname);
						Path[] listedFiles = FileUtil.stat2Paths(status);

						for (Path subFile : listedFiles) {
							if (subFile.getName().startsWith(bucketFilePrefix)
									&& StringUtils.isNumeric(subFile.getName().substring(bucketFilePrefix.length()))) {
								String path = pathname.getName() + File.separator + subFile.getName();
								hdfsBuckets.get().put(convertToSequence(path), path);
							}
						}
					}
				}

			}
		}
	}

	private static class PathSequenceComparator implements Comparator<Sequence> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Sequence o1, Sequence o2) {
			if (o1.getCreationDate() > o2.getCreationDate()) {
				return 1;
			} else if (o1.getCreationDate() < o2.getCreationDate()) {
				return -1;
			} else {
				if (o1.getNumber() > o2.getNumber()) {
					return 1;
				} else if (o1.getNumber() < o2.getNumber()) {
					return -1;
				} else {
					return 0;
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#hasNexReadBucket(long)
	 */
	@Override
	public boolean hasNexReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence.clearOffset();
		NavigableMap<Sequence, String> localTailMap = localBuckets.get().tailMap(sequence, false);
		NavigableMap<Sequence, String> hdfsTailMap = hdfsBuckets.get().tailMap(sequence, false);

		if (localTailMap.isEmpty() && hdfsTailMap.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.puma.storage.BucketManager#close()
	 */
	@Override
	public void close() {
		stopped = true;
	}
}
