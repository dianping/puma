package com.dianping.puma.syncserver.util.mysql;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class MysqlRandom {

	public static Integer randomInteger(int min, int max) {
		return RandomUtils.nextInt(min, max);
	}

	public static Integer randomInteger(int bits) {
		return randomInteger(0, (int) Math.pow(10, bits) - 1);
	}

	public static String randomVarchar(int bits) {
		return RandomStringUtils.randomAlphanumeric(bits);
	}
}
