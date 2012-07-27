/**
 * Project: puma-server
 * 
 * File Created at 2012-7-27
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.server;

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

import org.apache.log4j.Logger;

import com.dianping.puma.bo.PositionInfo;

/**
 * TODO Comment of MMapBasedBinlogPositionHolder
 * 
 * @author Leo Liang
 * 
 */
public class MMapBasedBinlogPositionHolder implements BinlogPositionHolder {

	private static final Logger					log						= Logger.getLogger(MMapBasedBinlogPositionHolder.class);

	private final Map<String, PositionInfo>		positionFile			= new ConcurrentHashMap<String, PositionInfo>();
	private final Map<String, MappedByteBuffer>	mappedByteBufferMapping	= new ConcurrentHashMap<String, MappedByteBuffer>();
	private static final String					SUFFIX					= ".pumaconf";
	private static final String					PREFIX					= "server-";
	private static final long					DEFAULT_BINLOGPOS		= 4L;
	private static final int					MAX_FILE_SIZE			= 200;
	private static final byte[]					BUF_MASK				= new byte[MAX_FILE_SIZE];
	private File								baseDir;

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

	public synchronized PositionInfo getPositionInfo(String serverName, String defaultBinlogFile, Long defaultBinlogPos) {
		PositionInfo posInfo = positionFile.get(serverName);
		if (posInfo == null) {
			savePositionInfo(serverName, new PositionInfo(defaultBinlogPos == null ? DEFAULT_BINLOGPOS
					: defaultBinlogPos, defaultBinlogFile));
		}
		return positionFile.get(serverName);
	}

	public synchronized void savePositionInfo(String serverName, PositionInfo positionInfor) {
		positionFile.put(serverName, positionInfor);
		saveToFile(serverName, positionInfor);
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
					new RandomAccessFile(f, "rwd").getChannel().map(MapMode.READ_WRITE, 0, MAX_FILE_SIZE));
			positionFile.put(fileName.substring(PREFIX.length(), fileName.lastIndexOf(SUFFIX)), posInfo);

		} catch (Exception e) {
			log.error("Read file " + f.getAbsolutePath() + " failed.", e);
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

	private synchronized void saveToFile(String serverName, PositionInfo positionInfor) {
		String path = new File(baseDir, getConfFileName(serverName)).getAbsolutePath();
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
					throw new RuntimeException("Create file(" + path + " failed.");
				}
			}
		}

		MappedByteBuffer mbb = mappedByteBufferMapping.get(path);
		mbb.position(0);
		mbb.put(BUF_MASK);
		mbb.position(0);
		mbb.put((positionInfor.getBinlogFileName() == null ? "" : positionInfor.getBinlogFileName()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(positionInfor.getBinlogPosition()).getBytes());
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
