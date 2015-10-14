package com.dianping.puma.log;

public final class LoggerLoader {

    static {
        try {
            Class.forName("org.apache.log4j.Hierarchy");
            CustomLog4jFactory.init();
        } catch (ClassNotFoundException e) {
        }
    }

    public static void init() {
    }

    private LoggerLoader() {
    }
}