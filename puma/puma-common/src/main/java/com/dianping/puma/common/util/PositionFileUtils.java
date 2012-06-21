package com.dianping.puma.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class PositionFileUtils {

	private static final Logger						log					= Logger.getLogger(PositionFileUtils.class);

	private static final Map<String, PositionInfo>	positionFile		= new ConcurrentHashMap<String, PositionInfo>();
	private static final String						PARENT_PATH			= "/data/applogs/puma/";
	private static final String						SUFFIX				= ".pumaconf";
	private static final long						DEFAULT_BINLOGPOS	= 4L;

	static {
		init();
	}

	private static void init() {
		File fileBase = new File(PARENT_PATH);
		if (!fileBase.exists()) {
			fileBase.mkdirs();
		}
		String[] configs = fileBase.list(new FilenameFilter() {

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
				loadFromFile(config.substring(0, config.lastIndexOf(SUFFIX)));
			}
		}
	}

	private PositionFileUtils() {

	}

	public static PositionInfo getPositionInfo(String serverName, String defaultBinlogFile) {
		PositionInfo posInfo = positionFile.get(serverName);
		if (posInfo == null) {
			synchronized (positionFile) {
				if (posInfo == null) {
					savePositionInfo(serverName, new PositionInfo(DEFAULT_BINLOGPOS, defaultBinlogFile));
				}
			}
		}
		return positionFile.get(serverName);
	}

	public static void savePositionInfo(String serverName, PositionInfo positionInfor) {
		positionFile.put(serverName, positionInfor);
		saveToFile(serverName, positionInfor);
	}

	private static void loadFromFile(String serverName) {
		File f = new File(PARENT_PATH + serverName + SUFFIX);

		FileReader fr = null;

		try {
			fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String binlogFileName = br.readLine();
			long binlogPosition = Long.parseLong(br.readLine());
			PositionInfo posInfo = new PositionInfo(binlogPosition, binlogFileName);
			positionFile.put(serverName, posInfo);

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
		}

	}

	private static void saveToFile(String serverName, PositionInfo positionInfor) {

		File f = new File(PARENT_PATH + serverName + SUFFIX);

		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(positionInfor.getBinlogFileName());
			bw.newLine();
			bw.write(String.valueOf(positionInfor.getBinlogPosition()));
			bw.newLine();

		} catch (Exception e) {
			log.error("Write file " + f.getAbsolutePath() + " failed.", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					log.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
		}

	}
}