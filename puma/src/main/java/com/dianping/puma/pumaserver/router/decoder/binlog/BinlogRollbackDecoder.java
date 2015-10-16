package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.BinlogRollback;
import com.dianping.puma.core.dto.binlog.request.BinlogRollbackRequest;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.exception.DecoderException;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogRollbackDecoder implements RequestDecoder {
    Pattern pattern = Pattern.compile("^/puma/binlog/rollback.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) throws DecoderException {
        BinlogRollbackRequest binlogRollbackRequest = new BinlogRollbackRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        binlogRollbackRequest.setClientName(params.get("clientName").get(0));
        binlogRollbackRequest.setToken(params.get("token").get(0));

        BinlogInfo binlogInfo = new BinlogInfo();

        if (params.containsKey("timestamp")) {
            binlogInfo.setTimestamp(Long.valueOf(params.get("timestamp").get(0)));
        }

        if (params.containsKey("binlogFile") &&
                params.containsKey("binlogPosition") &&
                params.containsKey("serverId")) {
            binlogInfo.setServerId(Long.valueOf(params.get("serverId").get(0)));
            binlogInfo.setBinlogFile(params.get("binlogFile").get(0));
            binlogInfo.setBinlogPosition(Long.valueOf(params.get("binlogPosition").get(0)));
            if (params.containsKey("eventIndex")) {
                binlogInfo.setEventIndex(Integer.valueOf(params.get("eventIndex").get(0)));
            }
        }

        BinlogRollback binlogRollback = new BinlogRollback();
        binlogRollback.setBinlogInfo(binlogInfo);
        binlogRollbackRequest.setBinlogRollback(binlogRollback);

        return binlogRollbackRequest;
    }
}
