package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.netty.entity.binlog.request.BinlogUnsubscriptionRequest;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import com.dianping.puma.pumaserver.router.decoder.exception.DecoderException;
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
	public Object decode(FullHttpRequest request) throws DecoderException {
		BinlogUnsubscriptionRequest binlogUnsubscriptionRequest = new BinlogUnsubscriptionRequest();
		Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

		if (!params.containsKey("clientName")) {
			throw new DecoderException("must contain `clientName` in `BinlogUnsubscriptionDecoder`");
		} else {
			binlogUnsubscriptionRequest.setClientName(params.get("clientName").get(0));
		}

		if (!params.containsKey("token")) {
			throw new DecoderException("must contain `token` in `BinlogUnsubscriptionDecoder`");
		} else {
			binlogUnsubscriptionRequest.setToken(params.get("token").get(0));
		}

		return binlogUnsubscriptionRequest;
	}
}
