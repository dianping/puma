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
package com.dianping.puma.server.mysql.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO Comment of MySQLUtils
 * 
 * @author Leo Liang
 * 
 */
public class MySQLUtils {
    public static boolean versionMeetsMinimum(int serverMajorVersion, int serverMinorVersion,
            int serverSubMinorVersion, int major, int minor, int subminor) {
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

    static long[] newHash(String password) {
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
    
}
