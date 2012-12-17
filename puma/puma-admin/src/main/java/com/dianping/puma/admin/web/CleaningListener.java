package com.dianping.puma.admin.web;

import java.io.BufferedReader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.io.IOUtils;

public class CleaningListener implements HttpSessionListener, ServletContextListener {

    public void sessionCreated(HttpSessionEvent event) {
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        // 关闭该session的中的reader
        BufferedReader oldReader = (BufferedReader) session.getAttribute("dumpReader");
        IOUtils.closeQuietly(oldReader);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
