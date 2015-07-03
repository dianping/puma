package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogAck;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BinlogAckDecoder implements RequestDecoder {

    Pattern pattern = Pattern.compile("^/puma/binlog/ack.*$");

    @Override
    public boolean match(FullHttpRequest request) {
        return pattern.matcher(request.getUri()).matches();
    }

    @Override
    public Object decode(FullHttpRequest request) {
        BinlogAck binlogAck = new BinlogAck();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();


        List<String> clientName = params.get("clientName");
        if (clientName == null || clientName.size() == 0) {
            throw new RuntimeException("must contain client name in binlog ack.");
        }
        binlogAck.setClientName(clientName.get(0));

        List<String> token = params.get("token");
        if (token == null || token.size() == 0) {
            throw new RuntimeException("must contain token in binlog ack.");
        }
        binlogAck.setToken(token.get(0));


        if (params.containsKey("binlogFile") && params.containsKey("binlogPosition")) {
            binlogAck.setBinlogInfo(
                    new BinlogInfo(params.get("binlogFile").get(0),
                            Long.valueOf(params.get("binlogPosition").get(0)))
            );
        } else {
            throw new RuntimeException("no binlog info given in binlog ack.");
        }

        return binlogAck;
    }
}
