package com.dianping.puma.storage.utils;

import com.dianping.puma.utils.PropertyKeyConstants;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class DateUtils {
    private DateUtils() {
    }

    private static final String NOW_TYPE_ACTUAL_VALUE = "now";

    private static final String NOW_TYPE_MOCK_VALUE = "mock";

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter();

    private static String nowType = NOW_TYPE_ACTUAL_VALUE;

    private static DateTime mockedNow;

    static {
        if (NOW_TYPE_MOCK_VALUE.equals(System.getProperty(PropertyKeyConstants.PUMA_DATE_NOW_TYPE))) {
            try {
                mockedNow = DateTime.parse(System.getProperty(PropertyKeyConstants.PUMA_DATE_NOW_VALUE), DATE_FORMATTER);
                nowType = NOW_TYPE_MOCK_VALUE;
            } catch (Exception ignore) {
            }
        }
    }

    public static final String getNextDayWithoutFuture(String dateStr) {
        DateTime date = DateTime.parse(dateStr, DATE_FORMATTER);
        date = date.withFieldAdded(DurationFieldType.days(), 1);
        DateTime now = getNow();
        return date.compareTo(now) > 0 ? null : date.toString(DATE_FORMATTER);
    }

    protected static final DateTime getNow() {
        if (NOW_TYPE_ACTUAL_VALUE.equals(nowType)) {
            return DateTime.now();
        } else {
            return mockedNow;
        }
    }

    public static final String getNowString() {
        return getNow().toString(DATE_FORMATTER);
    }

    public static final int getNowInteger() {
        DateTime date = getNow();
        return date.getYear() * 10000 + date.getMonthOfYear() * 100 + date.getDayOfMonth();
    }
}
