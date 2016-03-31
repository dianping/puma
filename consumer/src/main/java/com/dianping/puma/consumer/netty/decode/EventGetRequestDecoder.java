package com.dianping.puma.consumer.netty.decode;

import com.dianping.puma.common.model.message.EventGetRequest;
import com.dianping.puma.consumer.exception.PumaEventRequestDecodeException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class EventGetRequestDecoder implements EventRequestDecoder {

    private final Pattern uriPattern = Pattern.compile("^/puma/binlog/get.*$");

    private boolean defaultAutoAck = false;

    private int defaultBatchSize = 1;

    private long defaultTimeout = 0;

    private TimeUnit defaultTimeUnit = TimeUnit.MILLISECONDS;

    @Override
    public Object decode(FullHttpRequest request) {
        String uri = request.getUri();
        if (!uriPattern.matcher(uri).matches()) {
            throw new PumaEventRequestDecodeException("Illegal event get request[%s].", request);
        }

        EventGetRequest eventGetRequest = new EventGetRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        eventGetRequest.setClientName(params.get("clientName").get(0));

        eventGetRequest.setToken(params.get("token").get(0));

        if (!params.containsKey("autoAck")) {
            eventGetRequest.setAutoAck(defaultAutoAck);
        } else {
            eventGetRequest.setAutoAck(Boolean.valueOf(params.get("defaultAutoAck").get(0)));
        }

        if (!params.containsKey("batchSize")) {
            eventGetRequest.setBatchSize(defaultBatchSize);
        } else {
            eventGetRequest.setBatchSize(Integer.valueOf(params.get("batchSize").get(0)));
        }

        if (!params.containsKey("timeout")) {
            eventGetRequest.setTimeout(defaultTimeout);
        } else {
            eventGetRequest.setTimeout(Long.valueOf(params.get("timeout").get(0)));
        }

        if (!params.containsKey("timeUnit")) {
            eventGetRequest.setTimeUnit(defaultTimeUnit);
        } else {
            eventGetRequest.setTimeUnit(TimeUnit.valueOf(params.get("timeUnit").get(0)));
        }

        return eventGetRequest;
    }
}
