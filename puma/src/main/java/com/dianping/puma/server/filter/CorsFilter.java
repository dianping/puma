package com.dianping.puma.server.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Dozer @ 15/8/27
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component(value = "corsFilter")
public class CorsFilter extends OncePerRequestFilter implements Filter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        filterChain.doFilter(request, response);
    }
}