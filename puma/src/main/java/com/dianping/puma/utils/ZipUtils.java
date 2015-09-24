package com.dianping.puma.utils;

import java.io.*;

public class ZipUtils {

	public static boolean checkGZip(File file) throws FileNotFoundException {
		InputStream is = new FileInputStream(file);
		byte[] signature = new byte[2];
		try {
			int readable = is.read(signature);
			if (readable != 2) {
				return false;
			}
			return signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b;
		} catch (IOException io) {
			return false;
		} finally {
			try {
				is.close();
			} catch (IOException ignore) {
			}
		}
	}
}
