package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogAckRequest;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.exception.DecoderException;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
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
    public Object decode(FullHttpRequest request) throws DecoderException {
        BinlogAckRequest binlogAckRequest = new BinlogAckRequest();
        Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

        binlogAckRequest.setClientName(params.get("clientName").get(0));

        binlogAckRequest.setToken(params.get("token").get(0));

        BinlogAck binlogAck = new BinlogAck();
        BinlogInfo binlogInfo = new BinlogInfo(
                Long.valueOf(params.get("serverId").get(0)),
                params.get("binlogFile").get(0),
                Long.valueOf(params.get("binlogPosition").get(0)),
                0,
                Long.valueOf(params.get("timestamp").get(0))
        );
        if(params.containsKey("eventIndex")){
            binlogInfo.setEventIndex(Integer.valueOf(params.get("eventIndex").get(0)));
        }


        binlogAck.setBinlogInfo(binlogInfo);
        binlogAckRequest.setBinlogAck(binlogAck);

        return binlogAckRequest;
    }
}
