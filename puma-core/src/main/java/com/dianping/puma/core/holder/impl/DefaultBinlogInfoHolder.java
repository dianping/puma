package com.dianping.puma.core.holder.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.model.BinlogInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * TODO Comment of MMapBasedBinlogPositionHolder
 *
 * @author Leo Liang
 */
@Service("binlogInfoHolder")
public class DefaultBinlogInfoHolder implements BinlogInfoHolder {

	private static final Logger log = Logger.getLogger(DefaultBinlogInfoHolder.class);

	private final Map<String, BinlogInfo> binlogInfoMap = new ConcurrentHashMap<String, BinlogInfo>();

	private final Map<String, MappedByteBuffer> mappedByteBufferMapping = new ConcurrentHashMap<String, MappedByteBuffer>();

	private static final String SUFFIX = ".pumaconf";

	private static final String PREFIX = "server-";

	private static final long DEFAULT_BINLOGPOS = 4L;

	private static final int MAX_FILE_SIZE = 200;

	private static final byte[] BUF_MASK = new byte[MAX_FILE_SIZE];

	@Value("/data/appdatas/puma/binlog/")
	private File baseDir;

	@PostConstruct
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
	}

	public synchronized BinlogInfo getBinlogInfo(String taskName) {
		return binlogInfoMap.get(taskName);
	}

	public synchronized void setBinlogInfo(String taskName, BinlogInfo binlogInfo) {
		this.binlogInfoMap.put(taskName, binlogInfo);
		this.saveToFile(taskName, binlogInfo);
	}

	private void loadFromFile(String taskName) {
		String path = (new File(baseDir, taskName)).getAbsolutePath();
		File f = new File(path);

		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String binlogFile = br.readLine();
			String binlogPositionStr = br.readLine();
			long binlogPosition = binlogPositionStr == null ? DEFAULT_BINLOGPOS : Long.parseLong(binlogPositionStr);
			BinlogInfo binlogInfo = new BinlogInfo(binlogFile, binlogPosition);
			mappedByteBufferMapping.put(path,
					new RandomAccessFile(f, "rwd").getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
			binlogInfoMap.put(taskName.substring(PREFIX.length(), taskName.lastIndexOf(SUFFIX)), binlogInfo);

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

	private synchronized void saveToFile(String taskName, BinlogInfo binlogInfo) {
		String path = new File(baseDir, getConfFileName(taskName)).getAbsolutePath();

		if (!mappedByteBufferMapping.containsKey(path)) {
			File f = new File(path);
			if (!f.exists()) {
				try {
					if (!f.createNewFile()) {
						throw new RuntimeException("Can not create file(" + f.getAbsolutePath() + ")");
					}
					mappedByteBufferMapping.put(path,
							new RandomAccessFile(f, "rwd").getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
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

	private static String getConfFileName(String serverName) {
		return PREFIX + serverName + SUFFIX;
	}

	@Override
	public void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
	}

}
