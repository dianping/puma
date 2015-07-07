package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientAuthException;
import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.entity.binlog.response.BinlogAckResponse;
import com.dianping.puma.core.netty.entity.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.netty.entity.binlog.response.BinlogSubscriptionResponse;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class SinglePumaClient implements PumaClient {

    private static final Logger logger = LoggerFactory.getLogger(SinglePumaClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private final Gson gson = new Gson();

    private final String clientName;
    private final String remoteIp;
    private final int remotePort;
    private final String baseUrl;
    private final HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom()
                    .setConnectTimeout(60 * 1000)
                    .setSocketTimeout(10 * 60 * 1000)//long pull 模式必须设置一个比较长的超时时间
                    .build()).build();

    private volatile String token;

    public SinglePumaClient(String clientName, String remoteIp, int remotePort) {
        this.clientName = clientName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.baseUrl = String.format("http://%s:%d", remoteIp, remotePort);
        logger.info("Current puma client base url is: {}", baseUrl);
    }

    @Override
    public void connect() throws PumaClientException {

    }

    @Override
    public void disconnect() throws PumaClientException {
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException, PumaClientAuthException {
        return get(batchSize, 0, null);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, PumaClientAuthException {
        List<NameValuePair> parma = new ArrayList<NameValuePair>();
        parma.add(new BasicNameValuePair("clientName", clientName));
        parma.add(new BasicNameValuePair("token", this.token));
        parma.add(new BasicNameValuePair("batchSize", String.valueOf(batchSize)));
        parma.add(new BasicNameValuePair("timeout", String.valueOf(timeout)));
        if (timeUnit != null) {
            parma.add(new BasicNameValuePair("timeUnit", timeUnit.toString()));
        }
        return execute("/puma/binlog/get", parma, BinlogGetResponse.class).getBinlogMessage();
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException, PumaClientAuthException {
        BinlogMessage message = get(batchSize);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, PumaClientAuthException {
        BinlogMessage message = get(batchSize, timeout, timeUnit);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public void ack(BinlogInfo binlogInfo) throws PumaClientException, PumaClientAuthException {
        List<NameValuePair> parma = new ArrayList<NameValuePair>();
        parma.add(new BasicNameValuePair("clientName", clientName));
        parma.add(new BasicNameValuePair("token", this.token));
        parma.add(new BasicNameValuePair("binlogFile", binlogInfo.getBinlogFile()));
        parma.add(new BasicNameValuePair("binlogPosition", String.valueOf(binlogInfo.getBinlogPosition())));
        execute("/puma/binlog/get", parma, BinlogAckResponse.class);
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException, PumaClientAuthException {
        //todo:
    }

    @Override
    public void rollback() throws PumaClientException, PumaClientAuthException {
        //todo:
    }

    @Override
    public void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) throws PumaClientException {
        List<NameValuePair> parma = new ArrayList<NameValuePair>();
        parma.add(new BasicNameValuePair("clientName", clientName));
        parma.add(new BasicNameValuePair("database", database));
        parma.add(new BasicNameValuePair("dml", String.valueOf(dml)));
        parma.add(new BasicNameValuePair("ddl", String.valueOf(ddl)));
        parma.add(new BasicNameValuePair("transaction", String.valueOf(transaction)));
        for (String table : tables) {
            parma.add(new BasicNameValuePair("table", table));
        }

        BinlogSubscriptionResponse response;
        try {
            response = execute("/puma/binlog/subscribe", parma, BinlogSubscriptionResponse.class);
        } catch (PumaClientAuthException e) {
            throw new PumaClientException(e.getMessage(), e);
        }
        this.token = response.getToken();
    }


    protected <T> T execute(String path, List<NameValuePair> params, Class<T> clazz) throws PumaClientException, PumaClientAuthException {
        HttpResponse result;
        try {
            HttpGet get = new HttpGet(baseUrl + path + "?" + URLEncodedUtils.format(params, DEFAULT_CHARSET));
            result = httpClient.execute(get);
        } catch (Exception e) {
            throw new PumaClientException(e.getMessage(), e);
        }

        String json;
        try {
            json = EntityUtils.toString(result.getEntity());
        } catch (Exception e) {
            throw new PumaClientException(e.getMessage(), e);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new PumaClientAuthException(json);
        }

        return gson.fromJson(json, clazz);
    }
}
