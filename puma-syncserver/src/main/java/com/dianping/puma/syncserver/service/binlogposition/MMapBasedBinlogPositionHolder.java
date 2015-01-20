package com.dianping.puma.syncserver.service.binlogposition;

import com.dianping.puma.bo.PositionInfo;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMapBasedBinlogPositionHolder implements BinlogPositionHolder {

	private static final Logger log = LoggerFactory.getLogger(MMapBasedBinlogPositionHolder.class);

	private final Map<String, PositionInfo> positionFile = new ConcurrentHashMap<String, PositionInfo>();
	private final Map<String, MappedByteBuffer> mappedByteBufferMapping = new ConcurrentHashMap<String, MappedByteBuffer>();
	private static final String SUFFIX = ".pumaconf";
	private static final String PREFIX = "syncserver-";
	private static final long DEFAULT_BINLOGPOS = 4L;
	private static final int MAX_FILE_SIZE = 200;
	private static final byte[] BUF_MASK = new byte[MAX_FILE_SIZE];
	private File baseDir;

	@Override
	public void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
	}

	@Override
	public synchronized PositionInfo getPositionInfo(String syncserverName, String defaultBinlogFile, Long defaultBinlogPos) {
		PositionInfo posInfo = positionFile.get(syncserverName);
		if (posInfo == null) {
			savePositionInfo(syncserverName, new PositionInfo(defaultBinlogPos == null ? DEFAULT_BINLOGPOS
					: defaultBinlogPos, defaultBinlogFile));
			posInfo = positionFile.get(syncserverName);
		}
		return posInfo;
	}

	@Override
	public synchronized void savePositionInfo(String syncserverName, PositionInfo positionInfo) {
		positionFile.put(syncserverName, positionInfo);
		saveToFile(syncserverName, positionInfo);
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
				return name.endsWith(SUFFIX) && name.startsWith(PREFIX);
			}
		});
		if (configs != null) {
			for (String config : configs) {
				loadFromFile(config);
			}
		}
	}

	private void loadFromFile(String fileName) {
		String path = (new File(baseDir, fileName)).getAbsolutePath();
		File f = new File(path);

		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String binlogFileName = br.readLine();
			String binlogPositionStr = br.readLine();
			long binlogPosition = binlogPositionStr == null ? DEFAULT_BINLOGPOS : Long.parseLong(binlogPositionStr);
			PositionInfo posInfo = new PositionInfo(binlogPosition, binlogFileName);
			mappedByteBufferMapping.put(path,
					new RandomAccessFile(f, "rwd").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
			positionFile.put(fileName.substring(PREFIX.length(), fileName.lastIndexOf(SUFFIX)), posInfo);

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

	private synchronized void saveToFile(String syncserverName, PositionInfo positionInfo) {
		String path = new File(baseDir, getConfFileName(syncserverName)).getAbsolutePath();
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
		mbb.put((positionInfo.getBinlogFileName() == null ? "" : positionInfo.getBinlogFileName()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(positionInfo.getBinlogPosition()).getBytes());
		mbb.put("\n".getBytes());
	}

	private static String getConfFileName(String syncserverName) {
		return PREFIX + syncserverName + SUFFIX;
	}
}
