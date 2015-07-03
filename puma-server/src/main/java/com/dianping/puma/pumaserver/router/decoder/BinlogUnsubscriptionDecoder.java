package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogUnsubscription;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BinlogUnsubscriptionDecoder implements RequestDecoder {

    Pattern pattern = Pattern.compile("^/puma/binlog/unsubscribe.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) {
        BinlogUnsubscription binlogUnsubscription = new BinlogUnsubscription();

        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        binlogUnsubscription.setClientName(params.get("clientName").get(0));

        binlogUnsubscription.setToken(params.get("token").get(0));

        return binlogUnsubscription;
    }
}
