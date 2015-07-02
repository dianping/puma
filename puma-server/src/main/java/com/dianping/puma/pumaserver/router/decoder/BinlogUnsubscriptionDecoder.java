package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogUnsubscription;
import io.netty.handler.codec.http.FullHttpRequest;

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
		return binlogUnsubscription;
	}
}
