package com.dianping.puma.storage.index;

import com.dianping.puma.utils.LinkedProperties;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class DefaultReadIndexManager<K extends IndexKey, V extends IndexValue<K>> implements ReadIndexManager<K, V> {

	private final String L1_INDEX_FOLDER = "/data/appdatas/puma/binlogIndex/";

	private final String L1_INDEX_FILE = "l1index";

	private final String L1_INDEX_FILE_SUFFIX = ".l1idx";

	private final String L2_INDEX_FOLDER = "/data/appdatas/puma/binlogIndex/l2index/";

	private final String L2_INDEX_FILE_SUFFIX = ".l2idx";

	private IndexItemConverter<K> indexKeyConverter;

	private IndexItemConverter<V> indexValueConverter;

	public DefaultReadIndexManager(IndexItemConverter<K> indexKeyConverter, IndexItemConverter<V> indexValueConverter) {
		this.indexKeyConverter = indexKeyConverter;
		this.indexValueConverter = indexValueConverter;
	}

	@Override
	public V findFirst() throws IOException {
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
		if (l1Index.isEmpty()) {
			return null;
		} else {
			Map.Entry<K, String> firstEntry = FluentIterable.from(l1Index.entrySet()).first().get();
			LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(
					firstEntry.getValue(), getL2IndexFile(firstEntry.getValue()), this.indexValueConverter);
			bucket.start();
			return bucket.next();
		}
	}

	@Override
	public V findLatest() throws IOException {
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
		if (l1Index.isEmpty()) {
			return null;
		} else {
			Map.Entry<K, String> lastEntry = FluentIterable.from(l1Index.entrySet()).last().get();

			LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(
					lastEntry.getValue(), getL2IndexFile(lastEntry.getValue()), this.indexValueConverter);
			bucket.start();

			V next = null;
			try {
				while (true) {
					next = bucket.next();
				}
			} catch (EOFException ignore) {
			} finally {
				bucket.stop();
			}

			if (next != null) {
				return next;
			} else {
				return null;
			}
		}
	}

	@Override
	public V findByTime(K startKey, boolean startWithCompleteTransaction) throws IOException {
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

		if (l1Index.isEmpty()) {
			return null;
		}

		LinkedList<K> keys = Lists.newLinkedList(l1Index.keySet());
		K matches = null;
		for (int k = keys.size() - 1; k >= 0; k--) {
			if (keys.get(k).getTimestamp() < startKey.getTimestamp()) {
				matches = keys.get(k);
				break;
			}
		}

		if (matches == null) {
			return null;
		}

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(l1Index.get(matches), getL2IndexFile(l1Index.get(matches)),
				this.indexValueConverter);
		bucket.start();

		V next = null;
		LinkedList<V> entries = new LinkedList<V>();

		try {
			while (true) {
				next = bucket.next();

				if (next.getIndexKey().getTimestamp() >= startKey.getTimestamp()) {
					break;
				} else {
					entries.add(next);
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (startWithCompleteTransaction) {
			// 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
			V last = entries.pollLast();

			while (last != null) {
				if (last.isTransactionCommit()) {
					return last;
				} else {
					last = entries.pollLast();
				}
			}
			// 如果仍然没有找到
			return findFromPreviousIndexBucketByTime(matches);
		} else {
			return entries.pollLast();
		}
	}

	@Override
	public V findByBinlog(K startKey, boolean startWithCompleteTransaction) throws IOException {
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

		if (l1Index.isEmpty()) {
			return null;
		}

		Map.Entry<K, String> target = null;

		for (Map.Entry<K, String> entry : l1Index.entrySet()) {
			K key = entry.getKey();

			if (key.getServerId() == startKey.getServerId()) {
				if (compareTo(startKey, key) >= 0) {

					if (target == null) {
						target = entry;
					} else {
						if (compareTo(key, target.getKey()) >= 0) {
							target = entry;
						}
					}
				}
			}
		}

		if (target == null) {
			return null;
		}

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(target.getValue(), getL2IndexFile(target.getValue()),
				this.indexValueConverter);
		bucket.start();

		V next = null;
		LinkedList<V> entries = new LinkedList<V>();

		try {
			while (true) {
				next = bucket.next();

				if (compareTo(next.getIndexKey(), startKey) > 0) {
					break;
				} else {
					entries.add(next);
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (startWithCompleteTransaction) {
			// 从队列中查找上一个transaction commit位置，从这个点之后的一定是完整的
			V last = entries.pollLast();

			while (last != null) {
				if (last.isTransactionCommit()) {
					return last;
				} else {
					last = entries.pollLast();
				}
			}

			// 如果仍然没有找到
			return findFromPreviousIndexBucketByBinlog(target.getKey());
		} else {
			return entries.pollLast();
		}
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

	protected void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected V findFromPreviousIndexBucketByTime(K startKey) throws IOException {
		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();
		if (l1Index.isEmpty()) {
			return null;
		}

		LinkedList<K> keys = Lists.newLinkedList(l1Index.keySet());
		int index = keys.indexOf(startKey);

		if (index == 0) {
			return null;
		}

		K lastKey = keys.get(index - 1);

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(l1Index.get(lastKey), getL2IndexFile(l1Index.get(lastKey)),
				this.indexValueConverter);
		bucket.start();

		V commitValue = null;

		try {
			while (true) {
				V next = bucket.next();

				if (next.isTransactionCommit()) {
					commitValue = next;
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (commitValue != null) {
			return commitValue;
		} else {
			return findFromPreviousIndexBucketByTime(lastKey);
		}
	}

	protected V findFromPreviousIndexBucketByBinlog(K startKey) throws IOException {
		Map.Entry<K, String> target = null;

		LinkedHashMap<K, String> l1Index = loadLinkedL1Index();

		for (Map.Entry<K, String> entry : l1Index.entrySet()) {
			K key = entry.getKey();

			if (key.getServerId() == startKey.getServerId()) {
				if (compareTo(startKey, key) > 0) {

					if (target == null) {
						target = entry;
					} else {
						if (compareTo(key, target.getKey()) > 0) {
							target = entry;
						}
					}
				}
			}
		}

		if (target == null) {
			return null;
		}

		LocalFileIndexBucket<K, V> bucket = new LocalFileIndexBucket<K, V>(target.getValue(), getL2IndexFile(target.getValue()),
				this.indexValueConverter);
		bucket.start();

		V commitValue = null;

		try {
			while (true) {
				V next = bucket.next();

				if (next.isTransactionCommit()) {
					commitValue = next;
				}
			}
		} catch (EOFException ignore) {
		} finally {
			bucket.stop();
		}

		if (commitValue != null) {
			return commitValue;
		} else {
			return findFromPreviousIndexBucketByBinlog(target.getKey());
		}
	}

	protected int compareTo(K key1, K key2) {
		if (key1.getServerId() == key2.getServerId()) {
			if (key1.getBinlogFile().equals(key2.getBinlogFile())) {
				if (key1.getBinlogPosition() == key2.getBinlogPosition()) {
					return 0;
				} else {
					return key1.getBinlogPosition() > key2.getBinlogPosition() ? 1 : -1;
				}
			} else {
				return key1.getBinlogFile().compareTo(key2.getBinlogFile());
			}
		} else {
			return key1.getServerId() > key2.getServerId() ? 1 : -1;
		}
	}

	protected File getL1IndexFile() {
		return new File(L1_INDEX_FOLDER, L1_INDEX_FILE + L1_INDEX_FILE_SUFFIX);
	}

	protected File getL2IndexFile(String l2IndexName) {
		return new File(L2_INDEX_FOLDER, l2IndexName + L2_INDEX_FILE_SUFFIX);
	}
}
