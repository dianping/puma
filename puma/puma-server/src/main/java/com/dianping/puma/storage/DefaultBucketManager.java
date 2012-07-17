package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.util.PumaThreadUtils;

public class DefaultBucketManager implements BucketManager {
	private static final Logger							log					= Logger.getLogger(DefaultBucketManager.class);
	private static final String							PATH_SEPARATOR		= "/";
	private String										bucketFilePrefix;
	private EventCodec									codec;
	private FileSystem									fileSystem;
	private String										hdfsBaseDir;
	private AtomicReference<TreeMap<Sequence, String>>	hdfsBuckets			= new AtomicReference<TreeMap<Sequence, String>>();

	private Configuration								hdfsConfig;
	private File										localBaseDir;
	private int											localBucketMaxSizeMB;

	private AtomicReference<TreeMap<Sequence, String>>	localBuckets		= new AtomicReference<TreeMap<Sequence, String>>();
	private volatile boolean							stopped				= false;
	private int											maxLocalFileCount;
	private List<String>								toBeArchiveBuckets	= Collections
																					.synchronizedList(new ArrayList<String>());

	public DefaultBucketManager(String localBaseDir, String hdfsBaseDir, String name, String bucketFilePrefix,
			int localBucketMaxSizeMB, EventCodec codec, int maxLocalFileCount) throws IOException {
		this.localBaseDir = new File(localBaseDir, name);
		this.localBucketMaxSizeMB = localBucketMaxSizeMB;
		this.bucketFilePrefix = bucketFilePrefix;
		this.codec = codec;
		this.hdfsBaseDir = hdfsBaseDir;
		this.maxLocalFileCount = maxLocalFileCount;
	}

	private void checkClosed() throws IOException {
		if (stopped) {
			throw new IOException("Bucket manager has been closed.");
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
		try {
			this.fileSystem.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private String convertToPath(Sequence seq) {
		return "20" + seq.getCreationDate() + PATH_SEPARATOR + bucketFilePrefix + seq.getNumber();
	}

	private Sequence convertToSequence(String path) {
		String[] parts = path.split(PATH_SEPARATOR);
		return new Sequence(Integer.valueOf(parts[0].substring(2)), Integer.valueOf(parts[1].substring(bucketFilePrefix
				.length())));
	}

	@Override
	public Bucket getNextReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence = sequence.clearOffset();
		NavigableMap<Sequence, String> localTailMap = localBuckets.get().tailMap(sequence, false);
		NavigableMap<Sequence, String> hdfsTailMap = hdfsBuckets.get().tailMap(sequence, false);

		if (!localTailMap.isEmpty()) {
			Entry<Sequence, String> firstEntry = localTailMap.firstEntry();
			File file = new File(localBaseDir, firstEntry.getValue());
			return new FileBucket(file, firstEntry.getKey(), localBucketMaxSizeMB, codec);
		} else if (!hdfsTailMap.isEmpty()) {
			Entry<Sequence, String> firstEntry = hdfsTailMap.firstEntry();
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

	private Sequence getNextWriteBucketSequence(Sequence seq) {
		if (getNowCreationDate() == seq.getCreationDate()) {
			return seq.getNext(false);
		} else {
			return seq.getNext(true);
		}
	}

	private int getNowCreationDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		return Integer.valueOf(sdf.format(new Date()));
	}

	@Override
	public Bucket getReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = null;
		String path = null;
		boolean hdfs = true; // flag whether hdfs or local bucket

		if (seq == -1L) {
			// find hdfs first
			if (!hdfsBuckets.get().isEmpty()) {
				path = hdfsBuckets.get().firstEntry().getValue();
				sequence = new Sequence(hdfsBuckets.get().firstEntry().getKey());
				hdfs = true;
			} else if (!localBuckets.get().isEmpty()) {
				path = localBuckets.get().firstEntry().getValue();
				sequence = new Sequence(localBuckets.get().firstEntry().getKey());
				hdfs = false;
			} else {
				throw new FileNotFoundException(String.format("No matching bucket for seq(%d)!", seq));
			}
		} else {
			sequence = new Sequence(seq);
			path = localBuckets.get().get(sequence);
			if (path == null) {
				path = hdfsBuckets.get().get(sequence);
				if (path != null) {
					hdfs = true;
				} else {
					throw new FileNotFoundException(String.format("No matching bucket for seq(%d)!", seq));
				}
			} else {
				hdfs = false;
			}

		}

		if (path != null) {
			int offset = sequence.getOffset();
			Bucket bucket = null;

			if (!hdfs) {
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
			throw new FileNotFoundException(String.format("Bucket not found for seq(%d)!", seq));
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

	public synchronized void init() throws Exception {
		initLocalBuckets();
		initHDFSBucket();

		startArchiveJob();
	}

	private void startArchiveJob() {
		Thread archiveThread = PumaThreadUtils.createThread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (stopped) {
						break;
					}

					try {

						archive();
						Thread.sleep(5 * 1000);
					} catch (Exception e) {
						log.error("Archive Job failed.", e);
					}

				}
			}
		}, "localToHdfsArchive", false);

		archiveThread.start();
	}

	private void archive() {
		if (localBuckets.get() != null && localBuckets.get().size() > maxLocalFileCount) {
			TreeMap<Sequence, String> bakLocalBuckets = localBuckets.get();
			TreeMap<Sequence, String> newLocalBuckets = new TreeMap<Sequence, String>();
			int i = 0;
			for (Entry<Sequence, String> entry : bakLocalBuckets.entrySet()) {
				if (i >= bakLocalBuckets.size() - maxLocalFileCount) {
					newLocalBuckets.put(entry.getKey(), entry.getValue());
				} else {
					toBeArchiveBuckets.add(entry.getValue());
				}
				i++;
			}
			localBuckets.set(newLocalBuckets);
		}

		if (toBeArchiveBuckets != null && toBeArchiveBuckets.size() > 0) {
			TreeMap<Sequence, String> newHdfsBuckets = new TreeMap<Sequence, String>(hdfsBuckets.get());

			Iterator<String> iterator = toBeArchiveBuckets.iterator();
			while (iterator.hasNext()) {
				String path = iterator.next();
				if (StringUtils.isNotBlank(path)) {
					if (doArchive(path)) {
						iterator.remove();
						newHdfsBuckets.put(convertToSequence(path), path);
					}
				}
			}

			hdfsBuckets.set(newHdfsBuckets);
		}
	}

	private boolean doArchive(String path) {
		try {
			File localFile = new File(localBaseDir, path);
			if (!localFile.exists()) {
				return true;
			}

			fileSystem.copyFromLocalFile(true, true, new Path(localBaseDir + PATH_SEPARATOR + path), new Path(
					hdfsBaseDir + path));

			File parent = localFile.getParentFile();
			if (parent != null) {
				String[] subFiles = parent.list();
				if (subFiles != null && subFiles.length == 0) {
					if (!parent.delete()) {
						log.warn("Delete folder(" + parent.getAbsolutePath() + ") failed.");
					}
				}
			}

			return true;
		} catch (Exception e) {
			log.error("Archive failed. path: " + path, e);
		}

		return false;
	}

	private void initHDFSBucket() throws IOException, URISyntaxException {
		initHdfsConfiguration();
		this.fileSystem = FileSystem.get(this.hdfsConfig);

		hdfsBuckets.set(new TreeMap<Sequence, String>(new PathSequenceComparator()));

		if (this.fileSystem.getFileStatus(new Path(this.hdfsBaseDir)).isDir()) {

			FileStatus[] dirsStatus = this.fileSystem.listStatus(new Path(this.hdfsBaseDir));
			if (dirsStatus == null || dirsStatus.length == 0) {
				return;
			}

			Path[] listedPaths = FileUtil.stat2Paths(dirsStatus);

			for (Path pathname : listedPaths) {

				if (this.fileSystem.getFileStatus(pathname).isDir()) {
					if (StringUtils.isNumeric(pathname.getName()) && pathname.getName().length() == 8) {

						FileStatus[] status = this.fileSystem.listStatus(pathname);
						Path[] listedFiles = FileUtil.stat2Paths(status);

						for (Path subFile : listedFiles) {
							if (subFile.getName().startsWith(bucketFilePrefix)
									&& StringUtils.isNumeric(subFile.getName().substring(bucketFilePrefix.length()))) {
								String path = pathname.getName() + PATH_SEPARATOR + subFile.getName();
								hdfsBuckets.get().put(convertToSequence(path), path);
							}
						}
					}
				}

			}
		}
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

	private void initLocalBuckets() {

		if (!localBaseDir.exists()) {
			if (!localBaseDir.mkdirs()) {
				throw new RuntimeException("Failed to make dir for " + localBaseDir.getAbsolutePath());
			}
		}
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
					String path = dir.getName() + PATH_SEPARATOR + subFile;
					localBuckets.get().put(convertToSequence(path), path);
				}
			}
		}
	}

	public void setHdfsBaseDir(String hdfsBaseDir) {
		this.hdfsBaseDir = hdfsBaseDir;
	}

	private static class PathSequenceComparator implements Comparator<Sequence>, Serializable {

		private static final long	serialVersionUID	= -350477869152651536L;

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
}
