package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class BinlogGetDecoder implements RequestDecoder {

    private static final boolean DEFAULT_AUTO_ACK = false;
    private static final int DEFAULT_BATCH_SIZE = 1;
    private static final long DEFAULT_TIMEOUT = 0;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    Pattern pattern = Pattern.compile("^/puma/binlog/get.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) {
        BinlogGetRequest binlogGetRequest = new BinlogGetRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        binlogGetRequest.setClientName(params.get("clientName").get(0));

        binlogGetRequest.setToken(params.get("token").get(0));

        if (!params.containsKey("autoAck")) {
            binlogGetRequest.setAutoAck(DEFAULT_AUTO_ACK);
        } else {
            binlogGetRequest.setAutoAck(Boolean.valueOf(params.get("autoAck").get(0)));
        }

        if (!params.containsKey("batchSize")) {
            binlogGetRequest.setBatchSize(DEFAULT_BATCH_SIZE);
        } else {
            binlogGetRequest.setBatchSize(Integer.valueOf(params.get("batchSize").get(0)));
        }

        if (!params.containsKey("timeout")) {
            binlogGetRequest.setTimeout(DEFAULT_TIMEOUT);
        } else {
            binlogGetRequest.setTimeout(Long.valueOf(params.get("timeout").get(0)));
        }

        if (!params.containsKey("timeUnit")) {
            binlogGetRequest.setTimeUnit(DEFAULT_TIME_UNIT);
        } else {
            binlogGetRequest.setTimeUnit(TimeUnit.valueOf(params.get("timeUnit").get(0)));
        }

        return binlogGetRequest;
    }
}
