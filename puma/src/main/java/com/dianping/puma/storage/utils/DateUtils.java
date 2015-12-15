package com.dianping.puma.storage.utils;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
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

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter();

    private volatile static DateTime mockedNow;

    public static void changeGetNowTime(String date) {
        if (Strings.isNullOrEmpty(date)) {
            mockedNow = null;
            return;
        }
        mockedNow = DateTime.parse(date, DATE_FORMATTER);
    }

    public static String getNextDayWithoutFuture(String dateStr) {
        DateTime date = DateTime.parse(dateStr, DATE_FORMATTER);
        date = date.withFieldAdded(DurationFieldType.days(), 1);
        DateTime now = getNow();
        return DateTimeComparator.getDateOnlyInstance().compare(date, now) > 0 ? null : date.toString(DATE_FORMATTER);
    }

    protected static DateTime getNow() {
        if (mockedNow == null) {
            return DateTime.now();
        } else {
            return mockedNow;
        }
    }

    public static String getNowString() {
        return getNow().toString(DATE_FORMATTER);
    }

    public static int getNowInteger() {
        DateTime date = getNow();
        return date.getYear() * 10000 + date.getMonthOfYear() * 100 + date.getDayOfMonth();
    }

    public static boolean expired(String preDateStr, String postDateStr, int delta) {
        DateTime preDateTime = DateTime.parse(preDateStr, DATE_FORMATTER);
        DateTime postDateTime = DateTime.parse(postDateStr, DATE_FORMATTER);
        preDateTime = preDateTime.withFieldAdded(DurationFieldType.days(), delta);
        return DateTimeComparator.getDateOnlyInstance().compare(preDateTime, postDateTime) <= 0;
    }
}
