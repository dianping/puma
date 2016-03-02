/**
 * Project: ${puma-server.aid}
 * 
 * File Created at 2012-6-11 $Id$
 * 
 * Copyright 2010 dianping.com. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.puma.parser.mysql.utils;

import com.dianping.puma.utils.CodecUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * TODO Comment of MySQLUtils
 * 
 * @author Leo Liang
 */
public final class MySQLUtils {
	private MySQLUtils() {

	}

	public static boolean versionMeetsMinimum(int serverMajorVersion, int serverMinorVersion, int serverSubMinorVersion,
	      int major, int minor, int subminor) {
		if (serverMajorVersion >= major) {
			if (serverMajorVersion == major) {
				if (serverMinorVersion >= minor) {
					if (serverMinorVersion == minor) {
						return (serverSubMinorVersion >= subminor);
					}
					// newer than major.minor
					return true;
				}
				// older than major.minor
				return false;
			}
			// newer than major
			return true;
		}

		return false;
	}

	public static String newCrypt(String password, String seed) {
		byte b;
		double d;

		if ((password == null) || (password.length() == 0)) {
			return password;
		}

		long[] pw = newHash(seed);
		long[] msg = newHash(password);
		long max = 0x3fffffffL;
		long seed1 = (pw[0] ^ msg[0]) % max;
		long seed2 = (pw[1] ^ msg[1]) % max;
		char[] chars = new char[seed.length()];

		for (int i = 0; i < seed.length(); i++) {
			seed1 = ((seed1 * 3) + seed2) % max;
			seed2 = (seed1 + seed2 + 33) % max;
			d = (double) seed1 / (double) max;
			b = (byte) java.lang.Math.floor((d * 31) + 64);
			chars[i] = (char) b;
		}

		seed1 = ((seed1 * 3) + seed2) % max;
		seed2 = (seed1 + seed2 + 33) % max;
		d = (double) seed1 / (double) max;
		b = (byte) java.lang.Math.floor(d * 31);

		for (int i = 0; i < seed.length(); i++) {
			chars[i] ^= (char) b;
		}

		return new String(chars);
	}

	private static long[] newHash(String password) {
		long nr = 1345345333L;
		long add = 7;
		long nr2 = 0x12345671L;
		long tmp;

		for (int i = 0; i < password.length(); ++i) {
			if ((password.charAt(i) == ' ') || (password.charAt(i) == '\t')) {
				continue; // skip spaces
			}

			tmp = (0xff & password.charAt(i));
			nr ^= ((((nr & 63) + add) * tmp) + (nr << 8));
			nr2 += ((nr2 << 8) ^ nr);
			add += tmp;
		}

		long[] result = new long[2];
		result[0] = nr & 0x7fffffffL;
		result[1] = nr2 & 0x7fffffffL;

		return result;
	}

	public static String oldCrypt(String password, String seed) {
		long hp;
		long hm;
		long s1;
		long s2;
		long max = 0x01FFFFFF;
		double d;
		byte b;

		if ((password == null) || (password.length() == 0)) {
			return password;
		}

		hp = oldHash(seed);
		hm = oldHash(password);

		long nr = hp ^ hm;
		nr %= max;
		s1 = nr;
		s2 = nr / 2;

		char[] chars = new char[seed.length()];

		for (int i = 0; i < seed.length(); i++) {
			s1 = ((s1 * 3) + s2) % max;
			s2 = (s1 + s2 + 33) % max;
			d = (double) s1 / max;
			b = (byte) java.lang.Math.floor((d * 31) + 64);
			chars[i] = (char) b;
		}

		return new String(chars);
	}

	static long oldHash(String password) {
		long nr = 1345345333;
		long nr2 = 7;
		long tmp;

		for (int i = 0; i < password.length(); i++) {
			if ((password.charAt(i) == ' ') || (password.charAt(i) == '\t')) {
				continue;
			}

			tmp = password.charAt(i);
			nr ^= ((((nr & 63) + nr2) * tmp) + (nr << 8));
			nr2 += tmp;
		}

		return nr & ((1L << 31) - 1L);
	}

	public static byte[] scramble411(String password, String seed, String encoding) throws NoSuchAlgorithmException,
	      UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA");
		String passwordEncoding = encoding;

		byte[] passwordHashStage1 = md.digest((passwordEncoding == null || passwordEncoding.length() == 0) ? password
		      .getBytes() : password.getBytes(encoding));
		md.reset();

		byte[] passwordHashStage2 = md.digest(passwordHashStage1);
		md.reset();

		byte[] seedAsBytes = seed.getBytes("ASCII");
		md.update(seedAsBytes);
		md.update(passwordHashStage2);

		byte[] toBeXord = md.digest();

		int numToXor = toBeXord.length;

		for (int i = 0; i < numToXor; i++) {
			toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
		}

		return toBeXord;
	}

	private static final int DIGITS_PER_4BYTES = 9;

	private static final BigDecimal POSITIVE_ONE = BigDecimal.ONE;

	private static final BigDecimal NEGATIVE_ONE = new BigDecimal("-1");

	private static final int DECIMAL_BINARY_SIZE[] = { 0, 1, 1, 2, 2, 3, 3, 4, 4, 4 };

	public static short toYear(short value) {
		return (short) (1900 + value);
	}

	public static java.sql.Date toDate(int value) {
		int d = value % 32;
		value >>>= 5;
		int m = value % 16;
		int y = value >> 4;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(y, m - 1, d);
		return new java.sql.Date(cal.getTimeInMillis());
	}

	public static java.sql.Time toTime(int value) {
		int s = value % 100;
		if (value <= 1) {
			s = 1;
		}
		value /= 100;
		int m = value % 100;
		int h = value / 100;
		Calendar c = Calendar.getInstance();
		c.set(70, 0, 1, h, m, s);
		return new java.sql.Time(c.getTimeInMillis());
	}

	public static String toTime2(int value, int nanos, int meta) {
		final int h = (value >> 12) & 0x3FF;
		final int m = (value >> 6) & 0x3F;
		final int s = (value >> 0) & 0x3F;

		String format = "%02d:%02d:%02d";
		return String.format(format, h, m, s) + microSecondToStr(nanos, meta);
	}

	public static String toDatetime(long value) {
		int sec = (int) (value % 100);
		if (value <= 1) {
			sec = 1;
		}
		value /= 100;
		int min = (int) (value % 100);
		value /= 100;
		int hour = (int) (value % 100);
		value /= 100;
		int day = (int) (value % 100);
		value /= 100;
		int mon = (int) (value % 100);
		int year = (int) (value / 100);
		if (mon > 0 && day > 0 && year > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			c.set(year, mon - 1, day, hour, min, sec);
			return sdf.format(c.getTime());
		} else {
			return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, mon, day, hour, min, sec);
		}
	}

	public static String toDatetime2(long value, int nanos, int meta) {
		final long x = (value >> 22) & 0x1FFFFL;
		final int year = (int) (x / 13);
		final int mon = (int) (x % 13);
		final int day = ((int) (value >> 17)) & 0x1F;
		final int hour = ((int) (value >> 12)) & 0x1F;
		final int min = ((int) (value >> 6)) & 0x3F;
		final int sec = ((int) (value >> 0)) & 0x3F;

		String format = "%04d-%02d-%02d %02d:%02d:%02d";

		return String.format(format, year, mon, day, hour, min, sec) + microSecondToStr(nanos, meta);
	}

	public static java.sql.Timestamp toTimestamp(long value) {
		if (value <= 1) {
			return new java.sql.Timestamp(1000);
		}
		return new java.sql.Timestamp(value * 1000L);
	}

	public static String toTimestamp2(long value, int nanos, int meta) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = new java.sql.Timestamp(value * 1000L);
		String strValue = sdf.format(time);
		return strValue + microSecondToStr(nanos, meta);
	}

	public static BigDecimal toDecimal(int precision, int scale, byte[] value) {
		boolean positive = (value[0] & 0x80) == 0x80;
		value[0] ^= 0x80;
		if (!positive) {
			for (int i = 0; i < value.length; i++) {
				value[i] ^= 0xFF;
			}
		}

		int x = precision - scale;
		int ipDigits = x / DIGITS_PER_4BYTES;
		int ipDigitsX = x - ipDigits * DIGITS_PER_4BYTES;
		int ipSize = (ipDigits << 2) + DECIMAL_BINARY_SIZE[ipDigitsX];
		int offset = DECIMAL_BINARY_SIZE[ipDigitsX];
		BigDecimal ip = offset > 0 ? BigDecimal.valueOf(CodecUtils.toInt(value, 0, offset)) : BigDecimal.ZERO;
		for (; offset < ipSize; offset += 4) {
			final int i = CodecUtils.toInt(value, offset, 4);
			ip = ip.movePointRight(DIGITS_PER_4BYTES).add(BigDecimal.valueOf(i));
		}

		int shift = 0;
		BigDecimal fp = BigDecimal.ZERO;
		for (; shift + DIGITS_PER_4BYTES <= scale; shift += DIGITS_PER_4BYTES, offset += 4) {
			final int i = CodecUtils.toInt(value, offset, 4);
			fp = fp.add(BigDecimal.valueOf(i).movePointLeft(shift + DIGITS_PER_4BYTES));
		}
		if (shift < scale) {
			final int i = CodecUtils.toInt(value, offset, DECIMAL_BINARY_SIZE[scale - shift]);
			fp = fp.add(BigDecimal.valueOf(i).movePointLeft(scale));
		}

		return positive ? POSITIVE_ONE.multiply(ip.add(fp)) : NEGATIVE_ONE.multiply(ip.add(fp));
	}

	public static int getDecimalBinarySize(int precision, int scale) {
		int x = precision - scale;
		int ipDigits = x / DIGITS_PER_4BYTES;
		int fpDigits = scale / DIGITS_PER_4BYTES;
		int ipDigitsX = x - ipDigits * DIGITS_PER_4BYTES;
		int fpDigitsX = scale - fpDigits * DIGITS_PER_4BYTES;
		return (ipDigits << 2) + DECIMAL_BINARY_SIZE[ipDigitsX] + (fpDigits << 2) + DECIMAL_BINARY_SIZE[fpDigitsX];
	}

	private static String microSecondToStr(int nanos, int meta) {
		if (meta > 6) {
			throw new IllegalArgumentException("unknow useconds meta : " + meta);
		}
		String microSecond = "";
		if (meta > 0) {
			microSecond = String.valueOf(nanos);
			if (microSecond.length() < meta) {
				int total = meta % 2 == 0 ? meta : meta + 1;
				int len = total - microSecond.length();
				StringBuilder prefixMicroSecond = new StringBuilder(len);
				for (; len > 0; len--) {
					prefixMicroSecond.append("0");
				}
				microSecond = prefixMicroSecond.toString() + microSecond;
			}
			microSecond = microSecond.substring(0, meta);
			return "." + microSecond;
		}
		return microSecond;

	}
}
