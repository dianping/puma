package com.dianping.puma.portal.web;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class Log4jMDCFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        StringBuilder uriWithQueryString = new StringBuilder(req.getRequestURI());
        String queryString = req.getQueryString();
        if (queryString != null) {
            uriWithQueryString.append("?").append(queryString);
        }
        MDC.put("req.uriWithQueryString", uriWithQueryString.toString());
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("req.uriWithQueryString");
        }
    }

    @Override
    public void destroy() {
    }
}
