package com.dianping.puma.common.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class SystemUtils {

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
}
