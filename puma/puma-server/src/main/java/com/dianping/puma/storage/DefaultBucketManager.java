package com.dianping.puma.storage;

import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

import com.dianping.puma.core.codec.EventCodec;

public class DefaultBucketManager implements BucketManager {
	private File										localBaseDir;
	private int											localBucketMaxSizeMB;
	private String										bucketFilePrefix;
	private EventCodec									codec;
	private volatile boolean							stopped			= false;

	private AtomicReference<TreeMap<Sequence, String>>	localBuckets	= new AtomicReference<TreeMap<Sequence, String>>();

	public DefaultBucketManager(String localBaseDir, String name, String bucketFilePrefix, int localBucketMaxSizeMB,
			EventCodec codec) {
		this.localBaseDir = new File(localBaseDir, name);
		this.localBucketMaxSizeMB = localBucketMaxSizeMB;
		this.bucketFilePrefix = bucketFilePrefix;
		this.codec = codec;
		init();
	}

	public synchronized void init() {
		if (!localBaseDir.exists()) {
			localBaseDir.mkdirs();
		}

		initLocalBuckets();
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
		if (seq == -1L) {
			// TODO find hdfs
			if (localBuckets.get().isEmpty()) {
				// TODO invalid seq
				path = null;
			} else {
				path = localBuckets.get().firstEntry().getValue();
				sequence = new Sequence(localBuckets.get().firstEntry().getKey());
			}
		} else {
			sequence = new Sequence(seq);
			path = localBuckets.get().get(sequence);
			if (path == null) {
				// TODO invalid seq
			}
		}

		if (path != null) {
			File file = new File(localBaseDir, path);
			int offset = sequence.getOffset();
			Bucket bucket = new FileBucket(file, sequence.clearOffset(), localBucketMaxSizeMB, codec);
			bucket.seek(offset);
			try {
				bucket.getNext();
			} catch (EOFException e) {
				// ignore
			}

			return bucket;
		} else {
			// TODO check HDFS bucket
			throw new FileNotFoundException(String.format("Bucket(%d) not found!", path));
		}

	}

	@Override
	public Bucket getNextReadBucket(long seq) throws IOException {
		checkClosed();
		Sequence sequence = new Sequence(seq);
		sequence = sequence.clearOffset();
		NavigableMap<Sequence, String> tailMap = localBuckets.get().tailMap(sequence, false);

		if (tailMap.isEmpty()) {
			throw new FileNotFoundException("No next read bucket for seq(" + seq + ")");
		} else {
			Entry<Sequence, String> firstEntry = tailMap.firstEntry();
			File file = new File(localBaseDir, firstEntry.getValue());
			return new FileBucket(file, firstEntry.getKey(), localBucketMaxSizeMB, codec);
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
		NavigableMap<Sequence, String> tailMap = localBuckets.get().tailMap(sequence, false);

		if (tailMap.isEmpty()) {
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
