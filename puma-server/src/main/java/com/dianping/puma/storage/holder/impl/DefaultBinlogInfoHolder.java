package com.dianping.puma.storage.holder.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.holder.BinlogInfoHolder;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Leo Liang
 */
public class DefaultBinlogInfoHolder implements BinlogInfoHolder {

	private static final Logger LOG = Logger.getLogger(DefaultBinlogInfoHolder.class);

	private final Map<String, BinlogInfo> binlogInfoMap = new ConcurrentHashMap<String, BinlogInfo>();

	private final Map<String, MappedByteBuffer> mappedByteBufferMapping = new ConcurrentHashMap<String, MappedByteBuffer>();

	private static final String SUFFIX = ".binlog";

	public static final long DEFAULT_BINLOGPOS = 4L;

	private static final int MAX_FILE_SIZE = 200;

	private static final byte[] BUF_MASK = new byte[MAX_FILE_SIZE];

	private File baseDir;

	private File bakDir;

	private File storageBakDir;

	private File masterStorageBaseDir;

	private File slaveStorageBaseDir;

	private File binlogIndexBaseDir;

	@PostConstruct
	public void init() {
		if (!baseDir.exists()) {
			if (!baseDir.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + baseDir.getAbsolutePath());
			}
		}

		if (!bakDir.exists()) {
			if (!bakDir.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + bakDir.getAbsolutePath());
			}
		}

		String[] configs = baseDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(SUFFIX)) {
					return true;
				}
				return false;
			}
		});
		if (configs != null) {
			for (String config : configs) {
				loadFromFile(file2task(config));
			}
		}
	}

	public synchronized BinlogInfo getBinlogInfo(String taskName) {
		return binlogInfoMap.get(taskName);
	}

	public synchronized void setBinlogInfo(String taskName, BinlogInfo binlogInfo) {
		this.binlogInfoMap.put(taskName, binlogInfo);
		this.saveToFile(taskName, binlogInfo);
	}

	public synchronized void remove(String taskName) {
		binlogInfoMap.remove(taskName);
		removeFile(taskName);
	}

	private void removeFile(String taskName) {
		String path = (new File(baseDir, task2file(taskName))).getAbsolutePath();

		// Remove from the file cache.
		mappedByteBufferMapping.remove(taskName);

		// Remove the file to the backup folder.
		File f = new File(path);
		if (f.exists() && f.renameTo(new File(bakDir, genBakFileName(taskName)))) {
			LOG.info("Remove bin log file success.");
		} else {
			LOG.warn("Remove bin log file failure.");
		}
	}

	@SuppressWarnings({ "resource", "unused" })
	private void loadFromFile(String taskName) {
		String path = (new File(baseDir, task2file(taskName))).getAbsolutePath();
		File f = new File(path);

		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String serverIdStr = br.readLine();
			String binlogFile = br.readLine();
			String binlogPositionStr = br.readLine();
			String eventIndexStr = br.readLine();

			/*
			 * long serverId = Long.valueOf(serverIdStr); long binlogPosition = binlogPositionStr == null ? DEFAULT_BINLOGPOS :
			 * Long.parseLong(binlogPositionStr); int eventIndex = Integer.valueOf(eventIndexStr).intValue(); BinlogInfo binlogInfo =
			 * new BinlogInfo(serverId, binlogFile, binlogPosition, eventIndex);
			 */
			BinlogInfo binlogInfo = new BinlogInfo(0, "mysql-bin.000000", 0L, 0);

			mappedByteBufferMapping.put(taskName,
			      new RandomAccessFile(f, "rwd").getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
			binlogInfoMap.put(taskName, binlogInfo);

		} catch (Exception e) {
			LOG.error("Read file " + f.getAbsolutePath() + " failed.", e);
			throw new RuntimeException("Read file " + f.getAbsolutePath() + " failed.", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					LOG.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
		}

	}

	private synchronized void saveToFile(String taskName, BinlogInfo binlogInfo) {
		String path = new File(baseDir, task2file(taskName)).getAbsolutePath();

		if (!mappedByteBufferMapping.containsKey(taskName)) {
			File f = new File(path);
			if (!f.exists()) {
				try {
					if (!f.createNewFile()) {
						throw new RuntimeException("Can not create file(" + f.getAbsolutePath() + ")");
					}
					mappedByteBufferMapping.put(taskName,
					      new RandomAccessFile(f, "rwd").getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
				} catch (IOException e) {
					throw new RuntimeException("Create file(" + path + " failed.", e);
				}
			}
		}

		MappedByteBuffer mbb = mappedByteBufferMapping.get(taskName);
		mbb.position(0);
		mbb.put(BUF_MASK);
		mbb.position(0);
		mbb.put(String.valueOf(binlogInfo.getServerId()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put((binlogInfo.getBinlogFile() == null ? "" : binlogInfo.getBinlogFile()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(binlogInfo.getBinlogPosition()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(binlogInfo.getEventIndex()).getBytes());
		mbb.put("\n".getBytes());
	}

	@Override
	public void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
	}

	@Override
	public void setBakDir(String bakDir) {
		this.bakDir = new File(bakDir);
	}

	@Override
	public void setStorageBakDir(String storageBakDir) {
		this.storageBakDir = new File(storageBakDir);
	}

	@Override
	public void setMasterStorageBaseDir(String masterStorageBaseDir) {
		this.masterStorageBaseDir = new File(masterStorageBaseDir);
	}

	@Override
	public void setSlaveStorageBaseDir(String slaveStorageBaseDir) {
		this.slaveStorageBaseDir = new File(slaveStorageBaseDir);
	}

	@Override
	public void setBinlogIndexBaseDir(String binlogIndexBaseDir) {
		this.binlogIndexBaseDir = new File(binlogIndexBaseDir);
	}

	private String task2file(String taskName) {
		return taskName + SUFFIX;
	}

	private String file2task(String filename) {
		return filename.substring(0, filename.indexOf(SUFFIX));
	}

	private String genBakFileName(String taskName) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return taskName + '-' + timestamp.getTime() + SUFFIX;
	}

	public void clean(final String taskName) {
		if (!storageBakDir.exists()) {
			if (!storageBakDir.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + storageBakDir.getAbsolutePath());
			}
		}

		String newBinlogIndex = "/binlogIndex/" + taskName;
		String newStorageMaster = "/storage/master/" + taskName;
		String newStorageSlave = "/storage/slave/" + taskName;

		File newBinlogIndexFolder = new File(storageBakDir, newBinlogIndex);
		File newStorageMasterFolder = new File(storageBakDir, newStorageMaster);
		File newStorageSlaveFolder = new File(storageBakDir, newStorageSlave);

		if (!newBinlogIndexFolder.exists()) {
			if (!newBinlogIndexFolder.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + newBinlogIndexFolder.getAbsolutePath());
			}
		}

		if (!newStorageMasterFolder.exists()) {
			if (!newStorageMasterFolder.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + newStorageMasterFolder.getAbsolutePath());
			}
		}

		if (!newStorageSlaveFolder.exists()) {
			if (!newStorageSlaveFolder.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + newStorageSlaveFolder.getAbsolutePath());
			}
		}

		// Bin log index.
		File file1 = new File(binlogIndexBaseDir.getAbsolutePath(), taskName);
		if (file1.exists() && file1.renameTo(newBinlogIndexFolder)) {
			LOG.info("Backup bin log index success.");
		}

		// Master.
		File file2 = new File(masterStorageBaseDir.getAbsolutePath(), taskName);
		if (file2.exists() && file2.renameTo(newStorageMasterFolder)) {
			LOG.info("Backup storage master success.");
		}

		// Slave.
		File file3 = new File(slaveStorageBaseDir.getAbsolutePath(), taskName);
		if (file3.exists() && file3.renameTo(newStorageSlaveFolder)) {
			LOG.info("Backup storage slave success.");
		}
	}
}
