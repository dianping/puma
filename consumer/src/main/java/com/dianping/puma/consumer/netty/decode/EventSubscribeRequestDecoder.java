package com.dianping.puma.consumer.netty.decode;

import com.dianping.puma.common.model.message.EventSubscribeRequest;
import com.dianping.puma.consumer.exception.PumaEventRequestUriException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by xiaotian.li on 16/3/14.
 * Email: lixiaotian07@gmail.com
 */
public class EventSubscribeRequestDecoder implements EventRequestDecoder {

    private boolean defaultDml = true;

    private boolean defaultDdl = false;

    private Pattern uriPattern = Pattern.compile("^/puma/binlog/subscribe.*$");

    @Override
    public Object decode(FullHttpRequest request) {
        String uri = request.getUri();
        if (!uriPattern.matcher(uri).matches()) {
            throw new PumaEventRequestUriException("Unmatched event rollback request[%s].", request.getUri());
        }

        EventSubscribeRequest eventSubscribeRequest = new EventSubscribeRequest();

        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        eventSubscribeRequest.setClientName(params.get("clientName").get(0));

        eventSubscribeRequest.setDatabase(params.get("database").get(0));

        List<String> tables = params.get("table");
        String tableRegex = "/" + StringUtils.join(tables, "|") + "/g";
        eventSubscribeRequest.setTableRegex(tableRegex);

        if (!params.containsKey("ddl")) {
            eventSubscribeRequest.setDdl(defaultDdl);
        } else {
            eventSubscribeRequest.setDdl(Boolean.valueOf(params.get("ddl").get(0)));
        }

        if (!params.containsKey("dml")) {
            eventSubscribeRequest.setDml(defaultDml);
        } else {
            eventSubscribeRequest.setDml(Boolean.valueOf(params.get("dml").get(0)));
        }

        return eventSubscribeRequest;
    }
}
