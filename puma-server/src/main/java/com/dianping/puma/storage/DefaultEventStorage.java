package com.dianping.puma.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.container.storage.StorageStateContainer;
import com.dianping.puma.core.model.state.Storage.StorageState;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.storage.exception.InvalidSequenceException;
import com.dianping.puma.storage.exception.StorageClosedException;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.exception.StorageWriteException;

public class DefaultEventStorage implements EventStorage {
	private static final Logger log = Logger
			.getLogger(DefaultEventStorage.class);

	private BucketManager bucketManager;

	private Bucket writingBucket;

	private EventCodec codec;

	private List<WeakReference<EventChannel>> openChannels = new ArrayList<WeakReference<EventChannel>>();

	private volatile boolean stopped = true;

	private BucketIndex masterBucketIndex;

	private BucketIndex slaveBucketIndex;

	private ArchiveStrategy archiveStrategy;

	private CleanupStrategy cleanupStrategy;

	private String name;

	private String taskName;

	private BinlogInfo binlogInfo;

	private static final String datePattern = "yyyy-MM-dd";

	private AtomicReference<String> lastDate = new AtomicReference<String>();

	private String binlogIndexBaseDir;

	private DataIndex<BinlogIndexKey, Long> binlogIndex;

	private AtomicReference<BinlogIndexKey> lastBinlogIndexKey = new AtomicReference<BinlogIndexKey>(
			null);

	private AtomicReference<Long> processingServerId = new AtomicReference<Long>(
			null);

	private String acceptedTablesConfigKey;

	private List<String> acceptedTables;

	private Map<String,AcceptedTables> acceptedDataTables;
	
	public void setAcceptedTablesConfigKey(String acceptedTablesConfigKey) {
		this.acceptedTablesConfigKey = acceptedTablesConfigKey;
	}

	/**
	 * @param binlogIndexBaseDir the binlogIndexBaseDir to set
	 */
	public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
		this.binlogIndexBaseDir = binlogIndexBaseDir;
	}

	/**
	 * @return the masterBucketIndex
	 */
	@Override
	public BucketIndex getMasterBucketIndex() {
		return masterBucketIndex;
	}

	/**
	 * @return the slaveBucketIndex
	 */
	@Override
	public BucketIndex getSlaveBucketIndex() {
		return slaveBucketIndex;
	}

	public void start() throws StorageLifeCycleException {
		stopped = false;
		masterBucketIndex.setMaster(true);
		slaveBucketIndex.setMaster(false);
		bucketManager = new DefaultBucketManager(masterBucketIndex,
				slaveBucketIndex, archiveStrategy, cleanupStrategy);
		binlogIndex = new DefaultDataIndexImpl<BinlogIndexKey, Long>(
				binlogIndexBaseDir, new LongIndexItemConvertor(),
				new BinlogIndexKeyConvertor());

		cleanupStrategy.addDataIndex(binlogIndex);

		initAcceptedTableList();

		StorageState storageState = new StorageState(name, taskName, binlogInfo);
		StorageStateContainer storageStateContainer = ComponentContainer.SPRING.lookup("storageStateContainer");
		storageStateContainer.add(storageState);

		try {
			masterBucketIndex.start();
			slaveBucketIndex.start();
			bucketManager.start();

			writingBucket = null;

			binlogIndex.start();
		} catch (Exception e) {
			throw new StorageLifeCycleException("Storage init failed", e);
		}
	}

	private void initAcceptedTableList() {
		if (StringUtils.isNotBlank(acceptedTablesConfigKey)) {
			try {
				String acceptedTablesStr = ConfigCache.getInstance()
						.getProperty(acceptedTablesConfigKey);

				acceptedTables = constructAcceptedTablesList(acceptedTablesStr);

				ConfigCache.getInstance().addChange(new ConfigChange() {

					@Override
					public void onChange(String key, String value) {
						if (acceptedTablesConfigKey.equals(key)) {
							acceptedTables = constructAcceptedTablesList(value);
						}
					}
				});

				return;

			} catch (LionException e) {
				log.warn(String.format("Get acceptedTablesConfig[%s] failed.",
						acceptedTablesConfigKey));
			}
		}

		acceptedTables = null;
	}

	private List<String> constructAcceptedTablesList(String acceptedTablesStr) {
		if (StringUtils.isNotBlank(acceptedTablesStr)) {
			String[] acceptedTablesArr = StringUtils.split(acceptedTablesStr,
					",");
			if (acceptedTablesArr != null && acceptedTablesArr.length > 0) {
				List<String> resList = new ArrayList<String>(
						acceptedTablesArr.length);
				for (String acceptedTable : acceptedTablesArr) {
					if (StringUtils.isNotBlank(acceptedTable)) {
						resList.add(StringUtils.trim(acceptedTable)
								.toLowerCase());
					}
				}
				log.info("accepted tables:" + resList);
				return resList;
			}
		}
		return null;
	}

	public CleanupStrategy getCleanupStrategy() {
		return cleanupStrategy;
	}

	/**
	 * @param cleanupStrategy the cleanupStrategy to set
	 */
	public void setCleanupStrategy(CleanupStrategy cleanupStrategy) {
		this.cleanupStrategy = cleanupStrategy;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public void setMasterBucketIndex(BucketIndex masterBucketIndex) {
		this.masterBucketIndex = masterBucketIndex;
	}

	public void setSlaveBucketIndex(BucketIndex slaveBucketIndex) {
		this.slaveBucketIndex = slaveBucketIndex;
	}

	/**
	 * @param archiveStrategy the archiveStrategy to set
	 */
	public void setArchiveStrategy(ArchiveStrategy archiveStrategy) {
		this.archiveStrategy = archiveStrategy;
	}

	@Override
	public EventChannel getChannel(long seq, long serverId, String binlog,
			long binlogPos, long timestamp) throws StorageException {
		long newSeq = translateSeqIfNeeded(seq, serverId, binlog, binlogPos,
				timestamp);
		EventChannel channel = new DefaultEventChannel(bucketManager, newSeq,
				codec, newSeq == seq);
		openChannels.add(new WeakReference<EventChannel>(channel));
		return channel;
	}

	/**
	 * @param codec the codec to set
	 */
	public void setCodec(EventCodec codec) {
		this.codec = codec;
	}

	public void setAcceptedDataTables(Map<String,AcceptedTables> acceptedDataTables) {
		this.acceptedDataTables = acceptedDataTables;
	}

	public Map<String,AcceptedTables> getAcceptedDataTables() {
		return acceptedDataTables;
	}

	@Override
	public synchronized void store(ChangedEvent event) throws StorageException {
		if (stopped) {
			throw new StorageClosedException("Storage has been closed.");
		}

		if (!needStoreNew(event)) {
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		String nowDate = sdf.format(new Date());

		if (processingServerId.get() == null) {
			processingServerId.set(event.getBinlogServerId());
		}

		if (lastDate.get() == null) {
			lastDate.set(nowDate);
		}

		try {
			boolean newL1Index = false;
			if (writingBucket == null) {
				writingBucket = bucketManager.getNextWriteBucket();
				newL1Index = true;
			} else if (!writingBucket.hasRemainingForWrite()) {
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				newL1Index = true;
			} else if (!processingServerId.get().equals(
					event.getBinlogServerId())) {
				writingBucket.stop();
				writingBucket = bucketManager.getNextWriteBucket();
				processingServerId.set(event.getBinlogServerId());
				newL1Index = true;
			} else {
				if (!lastDate.get().equals(nowDate)) {
					writingBucket.stop();
					writingBucket = bucketManager.getNextWriteBucket();
					lastDate.set(nowDate);
					newL1Index = true;
				}
			}

			long newSeq = writingBucket.getCurrentWritingSeq();
			updateIndex(event, newL1Index, newSeq);

			event.setSeq(newSeq);
			byte[] data = codec.encode(event);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(ByteArrayUtils.intToByteArray(data.length));
			bos.write(data);
			writingBucket.append(bos.toByteArray());
			bucketManager.updateLatestSequence(new Sequence(event.getSeq()));

			StorageStateContainer storageStateContainer = ComponentContainer.SPRING.lookup("storageStateContainer");
			storageStateContainer.setSeq(name, event.getSeq());
			storageStateContainer.setBinlogInfo(name, new BinlogInfo(event.getBinlog(), event.getBinlogPos()));

			SystemStatusContainer.instance.updateStorageStatus(name, event
					.getSeq());
		} catch (IOException e) {
			throw new StorageWriteException("Failed to write event.", e);
		}
	}

	private boolean needStore(ChangedEvent event) {
		if (acceptedTables == null || acceptedTables.isEmpty()) {
			return true;
		}

		if (event instanceof RowChangedEvent) {
			RowChangedEvent rce = (RowChangedEvent) event;

			if (StringUtils.isNotBlank(rce.getTable())) {
				if (log.isDebugEnabled()) {
					log.debug("table:" + rce.getTable().toLowerCase());
				}
				return acceptedTables.contains(rce.getTable().toLowerCase());
			}

			return true;
		} else {
			return true;
		}
	}
	
	private boolean needStoreNew(ChangedEvent event) {
		if (acceptedDataTables == null || acceptedDataTables.isEmpty()) {
			return true;
		}
		if (event instanceof RowChangedEvent||event instanceof DdlEvent) {
			if(StringUtils.isNotBlank(event.getDatabase())){
				if(acceptedDataTables.containsKey(event.getDatabase())){
					if (StringUtils.isNotBlank(event.getTable())) {
						if (log.isDebugEnabled()) {
							log.debug("table:" + event.getTable());
						}
						return acceptedDataTables.get(event.getDatabase()).isContains((event.getTable()));
					}
					return true;
				}
				return false;
			}
			if(event instanceof DdlEvent){
				log.info(event.toString());
			}
			return true;
		} else{
			return true;
		}
	}
	
	private void updateIndex(ChangedEvent event, boolean newL1Index, long newSeq)
			throws IOException {
		BinlogIndexKey binlogKey = new BinlogIndexKey(event.getBinlog(), event
				.getBinlogPos(), event.getBinlogServerId());

		if (newL1Index) {
			binlogIndex.addL1Index(binlogKey, writingBucket.getBucketFileName()
					.replace('/', '-'));
		}

		if (lastBinlogIndexKey.get() == null
				|| !lastBinlogIndexKey.get().equals(binlogKey)) {
			binlogIndex.addL2Index(binlogKey, newSeq);
			lastBinlogIndexKey.set(binlogKey);
		}
	}

	private long translateSeqIfNeeded(long seq, long serverId, String binlog,
			long binlogPos, long timestamp) throws InvalidSequenceException {
		if (seq == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
			if (serverId != -1L && binlog != null && binlogPos != -1L) {
				Long indexedSeq = binlogIndex.find(new BinlogIndexKey(binlog,
						binlogPos, serverId));
				if (indexedSeq != null) {
					seq = indexedSeq.longValue();
				} else {
					throw new InvalidSequenceException(
							String
									.format(
											"Invalid binlogInfo(serverId=%d, binlog=%s, binlogPos=%d)",
											serverId, binlog, binlogPos));
				}
			} else {
				throw new InvalidSequenceException(String.format(
						"Invalid sequence(seq=%d but no binlogInfo set)", seq));
			}
		} else if (seq == SubscribeConstant.SEQ_FROM_TIMESTAMP) {
			throw new UnsupportedOperationException();
		}
		return seq;
	}

	@Override
	public synchronized void stop() {
		if (stopped) {
			return;
		}
		stopped = true;
		try {
			bucketManager.stop();
		} catch (StorageLifeCycleException e1) {
			// ignore
		}
		if (writingBucket != null) {
			try {
				writingBucket.stop();
			} catch (IOException e) {
				// ignore
			}
		}

		try {
			binlogIndex.stop();
		} catch (IOException e1) {
			// ignore
		}

		for (WeakReference<EventChannel> channelRef : openChannels) {
			EventChannel channel = channelRef.get();
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

	}

}
