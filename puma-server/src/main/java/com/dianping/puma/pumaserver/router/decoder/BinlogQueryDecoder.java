package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dozer @ 6/25/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogQueryDecoder implements RequestDecoder {
    Pattern pattern = Pattern.compile("^/puma/channel/acceptor\\?.+$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) {
        BinlogQuery result = new BinlogQuery();

        String queryString = request.getUri().substring(request.getUri().indexOf("?") + 1);
        final Map<String, String> map = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(queryString);

        if (map.containsKey("seq")) {
            result.setSeq(Long.valueOf(map.get("seq")));
        } else {
            result.setSeq(-1);
        }

        if (map.containsKey("ddl")) {
            result.setDdl(Boolean.valueOf(map.get("ddl")));
        }

        if (map.containsKey("dml")) {
            result.setDml(Boolean.valueOf(map.get("dml")));
        }

        if (map.containsKey("ts")) {
            result.setTransaction(Boolean.valueOf(map.get("ts")));
        }

        result.setClientName(map.get("name"));

        result.setTarget(map.get("target"));

        if (map.containsKey("serverId")) {
            result.setServerId(Long.valueOf(map.get("serverId")));
        }

        if (map.containsKey("binlog") && map.containsKey("binlogPos")) {
            BinlogInfo binlogInfo = new BinlogInfo(map.get("binlog"), Long.valueOf(map.get("binlogPos")));
            result.setBinlogInfo(binlogInfo);
        }

        if (map.containsKey("timestamp")) {
            result.setTimestamp(Long.valueOf(map.get("timestamp")));
        }

        if (map.containsKey("dt")) {
            result.setDatabaseTables(Iterables.toArray(Splitter.on(',').trimResults().split(map.get("dt")), String.class));
        }

        return result;
    }
}
