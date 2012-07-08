package com.dianping.puma.utils;

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

import com.dianping.puma.bo.PositionInfo;

public class PositionFileUtils {

	private static final Logger						log					= Logger.getLogger(PositionFileUtils.class);

	private static final Map<String, PositionInfo>	positionFile		= new ConcurrentHashMap<String, PositionInfo>();
	private static final String						PARENT_PATH			= "/data/applogs/puma/";
	private static final String						SUFFIX				= ".pumaconf";
	private static final String						PREFIX				= "server-";
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

	private PositionFileUtils() {

	}

	public static PositionInfo getPositionInfo(String serverName, String defaultBinlogFile, Long defaultBinlogPos) {
		PositionInfo posInfo = positionFile.get(serverName);
		if (posInfo == null) {
			synchronized (positionFile) {
				if (posInfo == null) {
					savePositionInfo(serverName, new PositionInfo(defaultBinlogPos == null ? DEFAULT_BINLOGPOS
							: defaultBinlogPos, defaultBinlogFile));
				}
			}
		}
		return positionFile.get(serverName);
	}

	public static void savePositionInfo(String serverName, PositionInfo positionInfor) {
		positionFile.put(serverName, positionInfor);
		saveToFile(serverName, positionInfor);
	}

	private static void loadFromFile(String fileName) {
		File f = new File(PARENT_PATH + fileName);

		FileReader fr = null;

		try {
			fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String binlogFileName = br.readLine();
			String binlogPositionStr = br.readLine();
			long binlogPosition = binlogPositionStr == null ? DEFAULT_BINLOGPOS : Long.parseLong(binlogPositionStr);
			PositionInfo posInfo = new PositionInfo(binlogPosition, binlogFileName);
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
		}

	}

	private static void saveToFile(String serverName, PositionInfo positionInfor) {

		File f = new File(PARENT_PATH + getConfFileName(serverName));

		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(positionInfor.getBinlogFileName() == null ? "" : positionInfor.getBinlogFileName());
			bw.newLine();
			bw.write(String.valueOf(positionInfor.getBinlogPosition()));
			bw.newLine();
			bw.flush();

		} catch (Exception e) {
			log.error("Write file " + f.getAbsolutePath() + " failed.", e);
		} finally {
			if (fw != null) {
				try {
					fw.flush();
					fw.close();
				} catch (IOException e) {
					log.error("Close file " + f.getAbsolutePath() + " failed.");
				}
			}
		}

	}

	private static String getConfFileName(String serverName) {
		return PREFIX + serverName + SUFFIX;
	}
}