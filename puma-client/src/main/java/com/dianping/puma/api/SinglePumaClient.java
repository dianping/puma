package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.response.BinlogAckResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogUnsubscriptionResponse;
import com.dianping.puma.core.event.*;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.gson.*;
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

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class SinglePumaClient implements PumaClient {

    private static final Logger logger = LoggerFactory.getLogger(SinglePumaClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private final Gson gson;

    private volatile List<NameValuePair> subscribeRequest;

    private volatile String token;

    private final String clientName;
    private final String baseUrl;
    private final HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom()
                    .setConnectTimeout(60 * 1000)
                    .setSocketTimeout(10 * 60 * 1000)//long pull 模式必须设置一个比较长的超时时间
                    .build()).build();


    public SinglePumaClient(String clientName, String remoteIp, int remotePort) {
        this.gson = new GsonBuilder().registerTypeAdapter(Event.class, new EventJsonDeserializer()).create();
        this.clientName = clientName;
        this.baseUrl = String.format("http://%s:%d", remoteIp, remotePort);
        logger.info("Current puma client base url is: {}", baseUrl);
    }

    protected void checkConnection() throws PumaClientException {
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
        return execute("/puma/binlog/get", params, BinlogGetResponse.class).getBinlogMessage();
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
        addToken(params);
        execute("/puma/binlog/get", params, BinlogAckResponse.class);
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
        //todo:
    }

    @Override
    public void rollback() throws PumaClientException {
        //todo:
    }

    @Override
    public void unSubscribe() throws PumaClientException {
        this.token = null;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));
        addToken(params);
        execute("/puma/binlog/unsubscribe", params, BinlogUnsubscriptionResponse.class);
    }

    @Override
    public synchronized void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("clientName", clientName));
        params.add(new BasicNameValuePair("database", database));
        params.add(new BasicNameValuePair("dml", String.valueOf(dml)));
        params.add(new BasicNameValuePair("ddl", String.valueOf(ddl)));
        params.add(new BasicNameValuePair("transaction", String.valueOf(transaction)));
        for (String table : tables) {
            params.add(new BasicNameValuePair("table", table));
        }

        this.subscribeRequest = params;
        this.token = null;
    }

    protected void doSubscribe() throws PumaClientException {
        if (this.subscribeRequest == null) {
            throw new PumaClientException("Please subscribe first");
        }
        BinlogSubscriptionResponse response;
        response = execute("/puma/binlog/subscribe", this.subscribeRequest, BinlogSubscriptionResponse.class);
        this.token = response.getToken();
    }


    protected <T> T execute(String path, List<NameValuePair> params, Class<T> clazz) throws PumaClientException {
        checkConnection();

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
            this.token = null;
            throw new PumaClientException(e.getMessage(), e);
        }

        if (result.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            doSubscribe();
            addToken(params);
            return execute(path, params, clazz);
        }

        return gson.fromJson(json, clazz);
    }


    public class EventJsonDeserializer implements JsonDeserializer<Event> {
        @Override
        public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String eventType = jsonObject.get("eventType").getAsString();

            if (EventType.DDL.toString().equals(eventType)) {
                return context.deserialize(json, DdlEvent.class);
            } else if (EventType.DML.toString().equals(eventType)) {
                return context.deserialize(json, RowChangedEvent.class);
            } else if (EventType.ERROR.toString().equals(eventType)) {
                return context.deserialize(json, ServerErrorEvent.class);
            } else {
                throw new JsonParseException("Unknown EventType :" + eventType);
            }
        }
    }
}
