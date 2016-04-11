package com.dianping.puma.common.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class SystemUtils {

    private static File file = new File("/");

    private static Runtime runtime = Runtime.getRuntime();

    public static double getSystemLoadAverage() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return os.getSystemLoadAverage();
    }

    public static int getAvailableProcessors() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return os.getAvailableProcessors();
    }

    public static double getNormalizedLoadAverage() {
        return getSystemLoadAverage() / getAvailableProcessors();
    }

    public static long getTotalSpace() {
        return file.getTotalSpace();
    }

    public static long getFreeSpace() {
        return file.getFreeSpace();
    }

    public static long getUsableSpace() {
        return file.getUsableSpace();
    }

    public static double getSpaceUsage() {
        return 1 - (double) getUsableSpace() / (double) getTotalMemory();
    }

    public static long getTotalMemory() {
        return runtime.totalMemory();
    }

    public static long getFreeMemory() {
        return runtime.freeMemory();
    }

    public static double getMemoryUsage() {
        return 1 - (double) getFreeMemory() / (double) getTotalMemory();
    }
}
