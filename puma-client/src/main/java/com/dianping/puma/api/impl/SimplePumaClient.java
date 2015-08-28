package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.core.annotation.ThreadUnSafe;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.BinlogRollback;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogAckResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.GsonUtil;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ThreadUnSafe
public class SimplePumaClient implements PumaClient {

    private static final Logger logger = LoggerFactory.getLogger(SimplePumaClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private static final EventCodec codec = EventCodecFactory.createCodec("raw");

    private static final Gson gson = new Gson();

    private volatile BinlogSubscriptionRequest subscribeRequest;

    private volatile String token;

    private String pumaServerHost;

    private final String clientName;
    private final String baseUrl;
    private final HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(
                    RequestConfig.custom()
                            .setConnectTimeout(60 * 1000)
                            .setSocketTimeout(10 * 60 * 1000)
                            .build()).build();

    public SimplePumaClient(PumaClientConfig config) {
        this.pumaServerHost = config.getServerHost();
        this.clientName = config.getClientName();
        this.baseUrl = String.format("http://%s", config.getServerHost());
        logger.info("Current puma client base url is: {}", baseUrl);

        BinlogSubscriptionRequest subscriptionRequest = new BinlogSubscriptionRequest();
        subscriptionRequest.setCodec("raw");
        subscriptionRequest.setClientName(clientName);
        subscriptionRequest.setDatabase(config.getDatabase());
        subscriptionRequest.setDml(config.isDml());
        subscriptionRequest.setDdl(config.isDdl());
        subscriptionRequest.setTransaction(config.isTransaction());
        subscriptionRequest.setTables(config.getTables());

        this.subscribeRequest = subscriptionRequest;
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException {
        return get(batchSize, 0, null);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));
        params.add(new BasicNameValuePair("batchSize", String.valueOf(batchSize)));
        params.add(new BasicNameValuePair("timeout", String.valueOf(timeout)));
        if (timeUnit != null) {
            params.add(new BasicNameValuePair("timeUnit", timeUnit.toString()));
        }
        addToken(params);
        return execute("/puma/binlog/get", params, "GET", null, BinlogGetResponse.class).getBinlogMessage();
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
        BinlogMessage message = get(batchSize);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        BinlogMessage message = get(batchSize, timeout, timeUnit);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public void ack(BinlogInfo binlogInfo) throws PumaClientException {
        if (binlogInfo == null) {
            return;
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));
        params.add(new BasicNameValuePair("binlogFile", binlogInfo.getBinlogFile()));
        params.add(new BasicNameValuePair("binlogPosition", String.valueOf(binlogInfo.getBinlogPosition())));
        params.add(new BasicNameValuePair("serverId", String.valueOf(binlogInfo.getServerId())));
        params.add(new BasicNameValuePair("eventIndex", String.valueOf(binlogInfo.getEventIndex())));
        params.add(new BasicNameValuePair("timestamp", String.valueOf(binlogInfo.getTimestamp())));
        addToken(params);
        execute("/puma/binlog/ack", params, "GET", null, BinlogAckResponse.class);
    }

    protected void addToken(List<NameValuePair> parma) {
        Iterator<NameValuePair> iterator = parma.iterator();
        while (iterator.hasNext()) {
            NameValuePair nameValuePair = iterator.next();
            if (nameValuePair.getName().equals("token")) {
                iterator.remove();
            }
        }
        parma.add(new BasicNameValuePair("token", this.token));
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));

        if (binlogInfo != null) {
            params.add(new BasicNameValuePair("binlogFile", binlogInfo.getBinlogFile()));
            params.add(new BasicNameValuePair("binlogPosition", String.valueOf(binlogInfo.getBinlogPosition())));
            params.add(new BasicNameValuePair("serverId", String.valueOf(binlogInfo.getServerId())));
            params.add(new BasicNameValuePair("eventIndex", String.valueOf(binlogInfo.getEventIndex())));
            params.add(new BasicNameValuePair("timestamp", String.valueOf(binlogInfo.getTimestamp())));
        }

        addToken(params);
        execute("/puma/binlog/rollback", params, "GET", null, BinlogRollback.class);
    }

    @Override
    public void rollback() throws PumaClientException {
        rollback(null);
    }

    protected void doSubscribe() throws PumaClientException {
        if (this.subscribeRequest == null) {
            throw new PumaClientException("Please subscribe first");
        }
        BinlogSubscriptionResponse response;
        response = execute("/puma/binlog/subscribe", null, "POST", GsonUtil.toJson(this.subscribeRequest), BinlogSubscriptionResponse.class);
        this.token = response.getToken();
    }


    protected <T> T execute(String path, List<NameValuePair> params, String method, String json, Class<T> clazz) throws PumaClientException {
        if (Strings.isNullOrEmpty(this.token) && !clazz.equals(BinlogSubscriptionResponse.class)) {
            doSubscribe();
            addToken(params);
        }

        HttpResponse result;
        HttpUriRequest request = null;
        try {
            String url = baseUrl + path;
            if (params != null && params.size() > 0) {
                url += ("?" + URLEncodedUtils.format(params, DEFAULT_CHARSET));
            }
            URI uri = new URI(url);
            if ("post".equalsIgnoreCase(method)) {
                HttpPost post = new HttpPost(uri);
                post.setEntity(new StringEntity(json, "utf-8"));
                request = post;
            } else {
                request = new HttpGet(uri);
            }

            while (true) {
                try {
                    result = httpClient.execute(request);
                    break;
                } catch (SocketTimeoutException ignore) {
                    ignore.printStackTrace();
                }
            }
        } catch (Exception e) {
            this.token = null;
            String msg = request == null ? e.getMessage() : String.format("%s %s", request.getURI(), e.getMessage());
            throw new PumaClientException(msg, e);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            doSubscribe();
            addToken(params);
            return execute(path, params, method, json, clazz);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                if (clazz.equals(BinlogGetResponse.class) &&
                        result.getHeaders(HttpHeaders.CONTENT_TYPE)[0].getValue().equals(MediaType.OCTET_STREAM.toString())) {
                    BinlogGetResponse response = new BinlogGetResponse();
                    BinlogMessage message = new BinlogMessage();
                    message.setBinlogEvents(codec.decodeList(EntityUtils.toByteArray(result.getEntity())));
                    response.setBinlogMessage(message);
                    return (T) response;
                } else {
                    return gson.fromJson(EntityUtils.toString(result.getEntity()), clazz);
                }
            } catch (Exception e) {
                this.token = null;
                throw new PumaClientException(e.getMessage(), e);
            }
        } else {
            this.token = null;
            try {
                throw new PumaClientException(String.format("[HttpStatus:%d]%s", result.getStatusLine().getStatusCode(), EntityUtils.toString(result.getEntity())));
            } catch (IOException e) {
                throw new PumaClientException(String.format("[HttpStatus:%d]%s", result.getStatusLine().getStatusCode(), e.getMessage()), e);
            }
        }
    }

    public String getServerHost() {
        return pumaServerHost;
    }
}
