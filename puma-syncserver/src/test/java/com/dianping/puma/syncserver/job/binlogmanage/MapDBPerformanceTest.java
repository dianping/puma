package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import org.mapdb.*;

import java.io.File;
import java.util.Map;

public class MapDBPerformanceTest {

	public static void main(String args[]) {
		DB db = DBMaker.newFileDB(new File("/data/appdatas/puma/MapDBPerformanceTest")).closeOnJvmShutdown().make();
		Map<Integer, BinlogInfo> binlogInfoMap = db.getHashMap("MapDBPerformanceTest");

		long begin = System.currentTimeMillis();
		for (int i = 0; i != 10000; ++i) {
			binlogInfoMap.put(i, new BinlogInfo("mysql-bin.000001", (long) i));
		}
		long end = System.currentTimeMillis();

		System.out.println((end - begin));
	}
}
