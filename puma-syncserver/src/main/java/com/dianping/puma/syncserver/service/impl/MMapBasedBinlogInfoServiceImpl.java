package com.dianping.puma.syncserver.service.impl;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.syncserver.service.BinlogInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MMapBasedBinlogInfoServiceImpl implements BinlogInfoService {

	private static final Logger log = LoggerFactory.getLogger(MMapBasedBinlogInfoServiceImpl.class);

	private File baseDir;
	private File doneDir;
	private final Map<String, BinlogInfo> binlogInfoFile = new ConcurrentHashMap<String, BinlogInfo>();
	private final Map<String, MappedByteBuffer> mappedByteBufferMapping = new ConcurrentHashMap<String, MappedByteBuffer>();
	private static final int MAX_FILE_SIZE = 200;
	private static final byte[] BUF_MASK = new byte[MAX_FILE_SIZE];
	private static final String SUFFIX = ".binlog";
	private static final String PREFIX = "syncserver-";

	@Override
	public void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
	}

	@Override
	public synchronized BinlogInfo getBinlogInfo(String clientName) {
		return binlogInfoFile.get(getBinlogFileName(clientName));
	}

	@Override
	public synchronized void saveBinlogInfo(String clientName, BinlogInfo binlogInfo) {
		binlogInfoFile.put(getBinlogFileName(clientName), binlogInfo);
		saveToFile(clientName, binlogInfo);
	}

	public void init() {
		if (!baseDir.exists()) {
			if (!baseDir.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + baseDir.getAbsolutePath());
			}
		}
		String[] configs = baseDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(SUFFIX) && name.startsWith(PREFIX)) {
					return true;
				}
				return false;
			}
		});
		if (configs != null) {
			for (String config : configs) {
				loadFromFile(config);
			}
		}

		this.doneDir = new File(this.baseDir.getAbsolutePath() + "/done/");
		if (!doneDir.exists()) {
			if (!doneDir.mkdirs()) {
				throw new RuntimeException("Fail to make dir for " + doneDir.getAbsolutePath());
			}
		}
	}

	@Override
	public void removeBinlogInfo(String clientName) {
		String filename = getBinlogFileName(clientName);
		binlogInfoFile.remove(filename);
		mappedByteBufferMapping.remove(filename);

		File undoneFile = new File(baseDir, getBinlogFileName(clientName));
		File doneFile   = new File(doneDir, getBinlogDoneFileName(clientName));
		undoneFile.renameTo(doneFile);
		undoneFile.delete();
	}

	@Override
	public List<String> findSyncTaskClientNames() {
		List<String> syncTaskClientNames = new ArrayList<String>();
		List<String> binlogInfoFileNames = new ArrayList<String>();
		binlogInfoFileNames.addAll(binlogInfoFile.keySet());

		for (String filename: binlogInfoFileNames) {
			filename = filename.replace(SUFFIX, "");
			filename = filename.replace(PREFIX, "");
			syncTaskClientNames.add(filename);
		}

		return syncTaskClientNames;
	}

	private void loadFromFile(String filename) {
		String path = new File(baseDir, filename).getAbsolutePath();
		File f = new File(path);

		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String binlogFile = br.readLine();
			String binlogPositionStr = br.readLine();
			long binlogPosition = Long.parseLong(binlogPositionStr);
			BinlogInfo binlogInfo = new BinlogInfo();
			binlogInfo.setBinlogFile(binlogFile);
			binlogInfo.setBinlogPosition(binlogPosition);
			mappedByteBufferMapping.put(path, new RandomAccessFile(f, "rwd").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
			binlogInfoFile.put(filename, binlogInfo);
		} catch (Exception e) {
			log.error("Read file " + f.getAbsolutePath() + " failed.", e);
			throw new RuntimeException("Read file " + f.getAbsolutePath() + " failed.", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					log.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
		}
	}

	private void saveToFile(String clientName, BinlogInfo binlogInfo) {
		String path = new File(baseDir, getBinlogFileName(clientName)).getAbsolutePath();
		if (!mappedByteBufferMapping.containsKey(path)) {
			File f = new File(path);
			if (!f.exists()) {
				try {
					if (!f.createNewFile()) {
						throw new RuntimeException("Can not create file(" + f.getAbsolutePath() + ")");
					}
					mappedByteBufferMapping.put(path,
							new RandomAccessFile(f, "rwd").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
				} catch (IOException e) {
					throw new RuntimeException("Create file(" + path + " failed.", e);
				}
			}
		}

		MappedByteBuffer mbb = mappedByteBufferMapping.get(path);
		mbb.position(0);
		mbb.put(BUF_MASK);
		mbb.position(0);
		mbb.put((binlogInfo.getBinlogFile() == null ? "" : binlogInfo.getBinlogFile()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(binlogInfo.getBinlogPosition()).getBytes());
		mbb.put("\n".getBytes());
	}

	private static String getBinlogFileName(String clientName) {
		return PREFIX + clientName + SUFFIX;
	}

	private static String getBinlogDoneFileName(String clientName) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
		String date = formatter.format(System.currentTimeMillis());
		return PREFIX + clientName + SUFFIX + "." + date;
	}
}