package com.dianping.puma.storage.index;

import com.dianping.puma.utils.LinkedProperties;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultWriteIndexManager<K extends IndexKey, V extends IndexValue<K>> implements WriteIndexManager<K, V> {

	private String L1_INDEX_FOLDER = "/data/appdatas/puma/binlogIndex/";

	private final String L1_INDEX_FILE = "l1index";

	private final String L1_INDEX_FILE_SUFFIX = ".l1idx";

	private String L2_INDEX_FOLDER = "/data/appdatas/puma/binlogIndex/l2index/";

	private final String L2_INDEX_FILE_SUFFIX = ".l2idx";

	private static final int BUF_SIZE = 1024 * 10;

	private IndexItemConverter<K> indexKeyConverter;

	private IndexItemConverter<V> indexValueConverter;

	private BufferedWriter l1IndexWriter;

	private String writingl2IndexName = null;

	private DataOutputStream l2IndexWriter;

	private ReentrantReadWriteLock l1Lock = new ReentrantReadWriteLock();

	private ReentrantReadWriteLock.WriteLock l1WriteLock = l1Lock.writeLock();

	private ReentrantReadWriteLock l2Lock = new ReentrantReadWriteLock();

	private ReentrantReadWriteLock.WriteLock l2WriteLock = l2Lock.writeLock();

	private AtomicReference<K> latestL2Index = new AtomicReference<K>();

	public DefaultWriteIndexManager(IndexItemConverter<K> indexKeyConverter, IndexItemConverter<V> indexValueConverter) {
		this.indexKeyConverter = indexKeyConverter;
		this.indexValueConverter = indexValueConverter;
	}

	public DefaultWriteIndexManager(String l1IndexFolder, String l2IndexFolder,
			IndexItemConverter<K> indexKeyConverter, IndexItemConverter<V> indexValueConverter) {
		this.L1_INDEX_FOLDER = l1IndexFolder;
		this.L2_INDEX_FOLDER = l2IndexFolder;
		this.indexKeyConverter = indexKeyConverter;
		this.indexValueConverter = indexValueConverter;
	}

	@Override
	public void start() {
		try {
			createDirIfNeeded();

			File l1IndexFile = getL1IndexFile();
			l1IndexFile.createNewFile();
			l1IndexWriter = new BufferedWriter(new FileWriter(l1IndexFile, true));
		} catch (IOException e) {
			throw new RuntimeException("", e);
		}

		l2WriteLock.lock();
		try {
			writingl2IndexName = null;
			closeQuietly(l2IndexWriter);
			l2IndexWriter = null;
		} finally {
			l2WriteLock.unlock();
		}
	}

	@Override
	public void stop() {
		closeQuietly(l1IndexWriter);
		l1IndexWriter = null;
		closeQuietly(l2IndexWriter);
		writingl2IndexName = null;
		l2IndexWriter = null;
	}

	@Override
	public void addL1Index(K key, String l2IndexName) throws IOException {
		boolean added = false;
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
		l1WriteLock.lock();
		try {
			if (!l1Index.containsKey(key)) {
				appendL1IndexToFile(key, l2IndexName);
				added = true;
			}
		} finally {
			l1WriteLock.unlock();
		}

		l2WriteLock.lock();
		try {
			if (added && !StringUtils.equals(l2IndexName, writingl2IndexName)) {
				writingl2IndexName = l2IndexName;
				closeQuietly(l2IndexWriter);
				File l2IndexFile = getL2IndexFile(writingl2IndexName);
				l2IndexFile.createNewFile();
				l2IndexWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(l2IndexFile), BUF_SIZE));
			}
		} finally {
			l2WriteLock.unlock();
		}
	}

	@Override
	public void addL2Index(K key, V value) throws IOException {
		l2WriteLock.lock();
		try {
			if (l2IndexWriter != null) {
				Object object = indexValueConverter.convertToObj(value);

				if (object instanceof byte[]) {
					byte[] bytes = (byte[]) object;

					l2IndexWriter.writeInt(bytes.length);
					l2IndexWriter.write(bytes);
				}

				latestL2Index.set(key);
			}
		} finally {
			l2WriteLock.unlock();
		}
	}

	@Override
	public void removeByL2IndexName(String l2IndexName) throws IOException {
		boolean removed = false;
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

		l1WriteLock.lock();
		try {
			if (l1Index.containsValue(l2IndexName)) {
				K tobeRemoveKey = null;
				for (K key : l1Index.keySet()) {
					if (l2IndexName.equals(l1Index.get(key))) {
						tobeRemoveKey = key;
						break;
					}
				}

				if (tobeRemoveKey != null) {
					l1Index.remove(tobeRemoveKey);
					removed = true;
					closeQuietly(l1IndexWriter);
					l1IndexWriter = null;
					FileUtils.deleteQuietly(getL1IndexFile());
					l1IndexWriter = new BufferedWriter(new FileWriter(getL1IndexFile()));
					for (Map.Entry<K, String> entry : l1Index.entrySet()) {
						appendL1IndexToFile(entry.getKey(), entry.getValue());
					}
				}
			}
		} finally {
			l1WriteLock.unlock();
		}

		if (removed) {
			l2WriteLock.lock();
			try {
				if (StringUtils.equals(l2IndexName, writingl2IndexName)) {
					writingl2IndexName = null;
					closeQuietly(l2IndexWriter);
					l2IndexWriter = null;
				}
				File l2IndexFile = getL2IndexFile(l2IndexName);
				FileUtils.deleteQuietly(l2IndexFile);
			} finally {
				l2WriteLock.unlock();
			}
		}
	}

	@Override
	public void flush() throws IOException {
		if (this.l2IndexWriter != null) {
			this.l2IndexWriter.flush();
		}
	}

	@Override
	public IndexBucket<K, V> getIndexBucket(String fileName) throws IOException {
		File l2IndexFile = getL2IndexFile(fileName);
		LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(fileName, l2IndexFile,
				this.indexValueConverter);

		return localFileIndexBucket;
	}

	@Override
	public boolean hasNextIndexBucket(String fileName) throws IOException {
		LinkedList<String> files = Lists.newLinkedList(loadLinkedL1Index().values());

		if (files.isEmpty()) {
			return false;
		}

		int index = files.indexOf(fileName);
		if (index < 0) {
			return false;
		}

		return index < files.size() - 1;
	}

	@Override
	public IndexBucket<K, V> getNextIndexBucket(String fileName) throws IOException {
		LinkedList<String> files = Lists.newLinkedList(loadLinkedL1Index().values());

		if (files.isEmpty()) {
			return null;
		}

		int index = files.indexOf(fileName);
		if (index < 0 || index == files.size() + 1) {
			return null;
		}
		String l2Index = files.get(index + 1);

		if (l2Index == null) {
			return null;
		}

		File l2IndexFile = getL2IndexFile(l2Index);
		LocalFileIndexBucket<K, V> localFileIndexBucket = new LocalFileIndexBucket<K, V>(l2Index, l2IndexFile,
				this.indexValueConverter);

		return localFileIndexBucket;
	}

	private void appendL1IndexToFile(K key, String l2IndexName) throws IOException {
		l1IndexWriter.write(indexKeyConverter.convertToObj(key) + "=" + l2IndexName);
		l1IndexWriter.newLine();
		l1IndexWriter.flush();
	}

	protected LinkedHashMap<K, String> loadLinkedL1Index() throws IOException {
		LinkedHashMap<K, String> l1Index = new LinkedHashMap<K, String>();
		File l1IndexFile = getL1IndexFile();
		l1IndexFile.createNewFile();
		InputStream is = null;
		try {
			LinkedProperties prop = new LinkedProperties();
			is = new FileInputStream(l1IndexFile);
			prop.load(is);
			for (Map.Entry<Object, Object> entry : prop.entrySet()) {
				K key = indexKeyConverter.convertFromObj(entry.getKey());
				String value = String.valueOf(entry.getValue());
				if (key != null && value != null) {
					l1Index.put(key, value);
				}
			}
		} finally {
			closeQuietly(is);
		}

		return l1Index;
	}

	private void closeQuietly(Writer out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void closeQuietly(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void createDirIfNeeded() throws IOException {
		File folder = new File(L1_INDEX_FOLDER);
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new IOException(String.format("Create dir(%s) failed.", L1_INDEX_FOLDER));
			}
		}

		File l2IndexFolder = new File(L2_INDEX_FOLDER);
		if (!l2IndexFolder.exists()) {
			if (!l2IndexFolder.mkdirs()) {
				throw new IOException(String.format("Create dir(%s) failed.", l2IndexFolder.getAbsolutePath()));
			}
		}
	}

	protected File getL1IndexFile() {
		return new File(L1_INDEX_FOLDER, L1_INDEX_FILE + L1_INDEX_FILE_SUFFIX);
	}

	protected File getL2IndexFile(String l2IndexName) {
		return new File(L2_INDEX_FOLDER, l2IndexName + L2_INDEX_FILE_SUFFIX);
	}
}
