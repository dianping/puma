package com.dianping.puma.consumer.netty.decode;

import com.dianping.puma.common.model.message.EventUnsubscribeRequest;
import com.dianping.puma.consumer.exception.PumaEventRequestDecodeException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class EventUnsubscribeRequestDecoder implements EventRequestDecoder {

    private final Pattern uriPattern = Pattern.compile("^/puma/binlog/unsubscribe.*$");

    @Override
    public Object decode(FullHttpRequest request) {
        String uri = request.getUri();
        if (!uriPattern.matcher(uri).matches()) {
            throw new PumaEventRequestDecodeException("Illegal event unsubscribe request[%s].", request);
        }

        EventUnsubscribeRequest eventUnsubscribeRequest = new EventUnsubscribeRequest();

        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        eventUnsubscribeRequest.setClientName(params.get("clientName").get(0));

        eventUnsubscribeRequest.setToken(params.get("token").get(0));

        return eventUnsubscribeRequest;
    }
}
