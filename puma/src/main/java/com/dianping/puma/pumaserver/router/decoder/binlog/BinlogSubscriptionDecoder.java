package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.util.ConvertHelper;
import com.dianping.puma.pumaserver.exception.DecoderException;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BinlogSubscriptionDecoder implements RequestDecoder {

    private static final String DEFAULT_CODEC = "json";
    private static final boolean DEFAULT_DDL = false;
    private static final boolean DEFAULT_DML = true;
    private static final boolean DEFAULT_TRANSACTION = false;

    Pattern pattern = Pattern.compile("^/puma/binlog/subscribe.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) throws DecoderException {
        if (request.getMethod().equals(HttpMethod.POST)) {
            String json = request.content().toString(Charset.forName("utf-8"));
            return ConvertHelper.fromJson(json, BinlogSubscriptionRequest.class);
        }

        BinlogSubscriptionRequest binlogSubscriptionRequest = new BinlogSubscriptionRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        binlogSubscriptionRequest.setClientName(params.get("clientName").get(0));

        binlogSubscriptionRequest.setDatabase(params.get("database").get(0));

        if (!params.containsKey("table")) {
            throw new DecoderException("must contain `table` in `BinlogSubscriptionRequest`");
        } else {
            binlogSubscriptionRequest.setTables(params.get("table"));
        }

        if (!params.containsKey("codec")) {
            binlogSubscriptionRequest.setCodec(DEFAULT_CODEC);
        } else {
            binlogSubscriptionRequest.setCodec(params.get("codec").get(0));
        }

        if (!params.containsKey("ddl")) {
            binlogSubscriptionRequest.setDdl(DEFAULT_DDL);
        } else {
            binlogSubscriptionRequest.setDdl(Boolean.valueOf(params.get("ddl").get(0)));
        }

        if (!params.containsKey("dml")) {
            binlogSubscriptionRequest.setDml(DEFAULT_DML);
        } else {
            binlogSubscriptionRequest.setDml(Boolean.valueOf(params.get("dml").get(0)));
        }

        if (!params.containsKey("transaction")) {
            binlogSubscriptionRequest.setTransaction(DEFAULT_TRANSACTION);
        } else {
            binlogSubscriptionRequest.setTransaction(Boolean.valueOf(params.get("transaction").get(0)));
        }

        return binlogSubscriptionRequest;
    }
}
