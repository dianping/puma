package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogAck;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.regex.Pattern;

public class BinlogAckDecoder implements RequestDecoder {

	Pattern pattern = Pattern.compile("^/puma/binlog/ack/*$");

	@Override
	public boolean match(FullHttpRequest request) {
		return pattern.matcher(request.getUri()).matches();
	}

	@Override
	public Object decode(FullHttpRequest request) {
		BinlogAck binlogAck = new BinlogAck();
		return binlogAck;
	}
}
