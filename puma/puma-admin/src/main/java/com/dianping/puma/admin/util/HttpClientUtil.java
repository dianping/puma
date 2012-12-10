package com.dianping.puma.admin.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class HttpClientUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final DefaultHttpClient httpclient;

    static {
        HttpParams httpParams = new BasicHttpParams();
        // 设置最大连接数  
        ConnManagerParams.setMaxTotalConnections(httpParams, 500);
        // 设置获取连接的最大等待时间  
        ConnManagerParams.setTimeout(httpParams, 5000);
        // 设置每个路由最大连接数  
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(100);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
        // 设置连接超时时间  
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        // 设置读取超时时间  
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, registry);
        httpclient = new DefaultHttpClient(cm, httpParams);
    }

    public static String post(String url, List<? extends NameValuePair> nvps) throws IOException {
        long start = System.currentTimeMillis();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

        HttpEntity entity = null;
        try {
            HttpResponse response = httpclient.execute(httpPost);
            entity = response.getEntity();
            InputStream ins = entity.getContent();
            String result = IOUtils.toString(ins, "UTF-8");
            if (LOG.isDebugEnabled()) {
                LOG.debug("****** http client invoke (Post method), url: " + url + ", nameValuePair: " + nvps + ", result: "
                        + result);
            }
            return result;
        } finally {
            EntityUtils.consume(entity);
            httpPost.releaseConnection();
            LOG.info("****** http client invoke (Post method), url: " + url + ", nameValuePair: " + nvps + ", time: "
                    + String.valueOf(System.currentTimeMillis() - start) + "ms.");
        }
    }

    public static String get(String url, List<? extends NameValuePair> nvps) throws IOException {
        long start = System.currentTimeMillis();

        //构造nvps为queryString
        if (nvps != null && nvps.size() > 0) {
            String query = URLEncodedUtils.format(nvps, "UTF-8");
            url += "?" + query;
        }
        HttpGet httpGet = new HttpGet(url);

        HttpEntity entity = null;
        try {
            HttpResponse response = httpclient.execute(httpGet);
            entity = response.getEntity();
            InputStream ins = entity.getContent();
            String result = IOUtils.toString(ins, "UTF-8");
            if (LOG.isDebugEnabled()) {
                LOG.debug("****** http client invoke (Get method), url: " + url + ", nameValuePair: " + nvps + ", result: "
                        + result);
            }
            return result;
        } finally {
            EntityUtils.consume(entity);
            httpGet.releaseConnection();
            LOG.info("****** http client invoke (Get method), url: " + url + ", nameValuePair: " + nvps + ", time: "
                    + String.valueOf(System.currentTimeMillis() - start) + "ms.");
        }
    }
}
