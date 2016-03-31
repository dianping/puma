package com.dianping.puma.consumer.netty.decode;

import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.common.model.message.EventAckRequest;
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
public class EventAckRequestDecoder implements EventRequestDecoder {

    private Pattern uriPattern = Pattern.compile("^/puma/binlog/ack.*$");

    @Override
    public Object decode(FullHttpRequest request) {
        String uri = request.getUri();
        if (!uriPattern.matcher(uri).matches()) {
            throw new PumaEventRequestDecodeException("Illegal event ack request[%s].", request);
        }

        EventAckRequest eventAckRequest = new EventAckRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        eventAckRequest.setClientName(params.get("clientName").get(0));

        eventAckRequest.setToken(params.get("token").get(0));

        ClientAck clientAck = new ClientAck();
        clientAck.setServerId(Long.valueOf(params.get("serverId").get(0)));
        clientAck.setFilename(params.get("binlogFile").get(0));
        clientAck.setPosition(Long.valueOf(params.get("binlogPosition").get(0)));
        clientAck.setTimestamp(Long.valueOf(params.get("timestamp").get(0)));

        eventAckRequest.setClientAck(clientAck);

        return eventAckRequest;
    }
}
