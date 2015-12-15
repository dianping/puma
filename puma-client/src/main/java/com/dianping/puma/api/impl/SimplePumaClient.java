package com.dianping.puma.api.impl;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.BinlogRollback;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogAckResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogRollbackResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.log.LoggerLoader;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
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
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimplePumaClient implements PumaClient {

    static {
        LoggerLoader.init();
    }

    private static final Logger LOG = LoggerFactory.getLogger(SimplePumaClient.class);

    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("PumaClientEventLogger");

    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private static final String EVENT_LOG_LION_KEY = "puma.eventlog.clientlist";

    protected static final EventCodec CODEC = EventCodecFactory.createCodec("raw");

    private static final Gson GSON = new Gson();

    private static final int CONNECT_TIMEOUT = 10 * 1000;

    private static final int SOCKET_TIMEOUT = 10 * 60 * 1000;

    private static final String HTTP_GET = HttpGet.METHOD_NAME;

    private static final String HTTP_POST = HttpPost.METHOD_NAME;

    private static final String PAIR_NAME_TOKEN = "token";

    protected final BinlogSubscriptionRequest subscribeRequest;

    private final String clientName;

    private final String baseUrl;

    private final long threadId = Thread.currentThread().getId();

    private final String pumaServerHost;

    protected String token;

    private boolean enableEventLog;

    private final HttpClient httpClient;


    public SimplePumaClient(PumaClientConfig config) {
        this(config, HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(CONNECT_TIMEOUT)
                                .setSocketTimeout(SOCKET_TIMEOUT)
                                .build()).build());
    }

    public SimplePumaClient(PumaClientConfig config, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.pumaServerHost = config.getServerHost();
        this.clientName = config.getClientName();
        this.baseUrl = String.format("http://%s", config.getServerHost());
        LOG.info("Current puma client base url is: {}", baseUrl);

        BinlogSubscriptionRequest subscriptionRequest = new BinlogSubscriptionRequest();
        subscriptionRequest.setCodec("raw");
        subscriptionRequest.setClientName(clientName);
        subscriptionRequest.setDatabase(config.getDatabase());
        subscriptionRequest.setDml(config.isDml());
        subscriptionRequest.setDdl(config.isDdl());
        subscriptionRequest.setTransaction(config.isTransaction());
        subscriptionRequest.setTables(config.getTables());

        this.subscribeRequest = subscriptionRequest;

        if (config.isEnableEventLog()) {
            enableEventLog = true;
        } else {
            enableEventLog = false;
            initEventLogConfig();
        }
    }

    private void initEventLogConfig() {
        ConfigCache configCache = ConfigCache.getInstance();
        String config = configCache.getProperty(EVENT_LOG_LION_KEY);
        parseEventLogConfig(config);

        configCache.addChange(new PumaClientConfigChange(this));
    }

    private void parseEventLogConfig(String config) {
        enableEventLog = (!Strings.isNullOrEmpty(config)) &&
                Sets.newHashSet(config.split(",")).contains(clientName);
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException {
        return get(batchSize, 0, null);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        checkThreadNotSafe();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));
        params.add(new BasicNameValuePair("batchSize", String.valueOf(batchSize)));
        params.add(new BasicNameValuePair("timeout", String.valueOf(timeout)));
        if (timeUnit != null) {
            params.add(new BasicNameValuePair("timeUnit", timeUnit.toString()));
        }
        addToken(params);
        BinlogMessage result = execute("/puma/binlog/get", params, HTTP_GET, null, BinlogGetResponse.class).getBinlogMessage();
        logResult(result);
        return result;
    }

    private void logResult(BinlogMessage message) {
        if (!enableEventLog) {
            return;
        }

        for (com.dianping.puma.core.event.Event event : message.getBinlogEvents()) {
            if (event.getBinlogInfo() != null) {
                EVENT_LOGGER.info(String.valueOf(event.getBinlogInfo().hashCode()));
            }
        }
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
        checkThreadNotSafe();

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
        execute("/puma/binlog/ack", params, HTTP_GET, null, BinlogAckResponse.class);
    }

    protected void addToken(List<NameValuePair> parma) {
        Iterator<NameValuePair> iterator = parma.iterator();
        while (iterator.hasNext()) {
            NameValuePair nameValuePair = iterator.next();
            if (nameValuePair.getName().equals(PAIR_NAME_TOKEN)) {
                iterator.remove();
            }
        }
        parma.add(new BasicNameValuePair(PAIR_NAME_TOKEN, this.token));
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
        checkThreadNotSafe();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));

        if (binlogInfo != null) {
            params.add(new BasicNameValuePair("binlogFile", binlogInfo.getBinlogFile()));
            params.add(new BasicNameValuePair("binlogPosition", String.valueOf(binlogInfo.getBinlogPosition())));
            params.add(new BasicNameValuePair("serverId", String.valueOf(binlogInfo.getServerId())));
            params.add(new BasicNameValuePair("eventIndex", String.valueOf(binlogInfo.getEventIndex())));
            params.add(new BasicNameValuePair("timestamp", String.valueOf(binlogInfo.getTimestamp())));
        }

        execute("/puma/binlog/rollback", params, HTTP_GET, null, BinlogRollback.class);
    }

    protected void doSubscribe() throws PumaClientException {
        if (this.subscribeRequest == null) {
            throw new PumaClientException("Please subscribe first");
        }
        BinlogSubscriptionResponse response;
        response = execute("/puma/binlog/subscribe", null, HTTP_POST, ConvertHelper.toJson(this.subscribeRequest), BinlogSubscriptionResponse.class);
        this.token = response.getToken();
    }


    protected <T> T execute(String path, List<NameValuePair> params, String method, String body, Class<T> clazz) throws PumaClientException {
        if (needToSubscribe(clazz)) {
            doSubscribe();
            addToken(params);
        }

        HttpResponse result;
        HttpUriRequest request = null;

        try {
            request = buildRequest(path, method, params, body);
            result = httpClient.execute(request);
        } catch (Exception e) {
            this.token = null;
            String msg = request == null ? e.getMessage() : String.format("%s %s", request.getURI(), e.getMessage());
            throw new PumaClientException(msg, e);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            doSubscribe();
            addToken(params);
            return execute(path, params, method, body, clazz);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return decode(clazz, result);
            } catch (Exception e) {
                this.token = null;
                throw new PumaClientException(e.getMessage(), e);
            }
        }

        this.token = null;
        try {
            throw new PumaClientException(String.format("[HttpStatus:%d]%s", result.getStatusLine().getStatusCode(), EntityUtils.toString(result.getEntity())));
        } catch (IOException e) {
            throw new PumaClientException(String.format("[HttpStatus:%d]%s", result.getStatusLine().getStatusCode(), e.getMessage()), e);
        }
    }

    protected <T> boolean needToSubscribe(Class<T> clazz) {
        return Strings.isNullOrEmpty(this.token) &&
                !clazz.equals(BinlogSubscriptionResponse.class) &&
                !clazz.equals(BinlogRollbackResponse.class);
    }

    protected HttpUriRequest buildRequest(String path, String method, List<NameValuePair> params, String body) throws URISyntaxException {
        HttpUriRequest request;
        String url = baseUrl + path;
        if (params != null && params.size() > 0) {
            url += ("?" + URLEncodedUtils.format(params, DEFAULT_CHARSET));
        }
        URI uri = new URI(url);
        if (HTTP_POST.equalsIgnoreCase(method)) {
            HttpPost post = new HttpPost(uri);
            post.setEntity(new StringEntity(body, DEFAULT_CHARSET));
            request = post;
        } else {
            request = new HttpGet(uri);
        }
        return request;
    }

    protected <T> T decode(Class<T> clazz, HttpResponse result) throws IOException {
        if (clazz.equals(BinlogGetResponse.class) &&
                result.getHeaders(HttpHeaders.CONTENT_TYPE) != null &&
                result.getHeaders(HttpHeaders.CONTENT_TYPE).length != 0 &&
                result.getHeaders(HttpHeaders.CONTENT_TYPE)[0].getValue().equals(MediaType.OCTET_STREAM.toString())) {
            BinlogGetResponse response = new BinlogGetResponse();
            BinlogMessage message = new BinlogMessage();
            message.setBinlogEvents(CODEC.decodeList(EntityUtils.toByteArray(result.getEntity())));
            response.setBinlogMessage(message);
            return (T) response;
        } else {
            return GSON.fromJson(EntityUtils.toString(result.getEntity()), clazz);
        }
    }

    private void checkThreadNotSafe() throws PumaClientException {
        if (threadId != Thread.currentThread().getId()) {
            throw new PumaClientException("PumaClient is not thread safe!");
        }
    }

    public String getServerHost() {
        return pumaServerHost;
    }

    static class PumaClientConfigChange implements ConfigChange {
        private final WeakReference<SimplePumaClient> parent;

        public PumaClientConfigChange(SimplePumaClient client) {
            parent = new WeakReference<SimplePumaClient>(client);
        }

        @Override
        public void onChange(String key, String value) {
            SimplePumaClient client = parent.get();
            if (client == null) {
                ConfigCache.getInstance().removeChange(this);
            } else {
                if (EVENT_LOG_LION_KEY.equals(key)) {
                    client.parseEventLogConfig(value);
                }
            }
        }
    }
}
