package com.dianping.puma.pumaserver.router.decoder.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.pumaserver.router.decoder.RequestDecoder;
import com.dianping.puma.pumaserver.exception.DecoderException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BinlogSubscriptionDecoder implements RequestDecoder {

	private static final boolean DEFAULT_DDL          = false;
	private static final boolean DEFAULT_DML          = true;
	private static final boolean DEFAULT_TRANSACTION  = false;

	Pattern pattern = Pattern.compile("^/puma/binlog/subscribe.*$");

	@Override
	public boolean match(FullHttpRequest request) {
		return pattern.matcher(request.getUri()).matches();
	}

	@Override
	public Object decode(FullHttpRequest request) throws DecoderException {
		BinlogSubscriptionRequest binlogSubscriptionRequest = new BinlogSubscriptionRequest();
		Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

		if (!params.containsKey("clientName")) {
			throw new DecoderException("must contain `clientName` in `BinlogSubscriptionRequest`");
		} else {
			binlogSubscriptionRequest.setClientName(params.get("clientName").get(0));
		}

		if (!params.containsKey("database")) {
			throw new DecoderException("must contain `database` in `BinlogSubscriptionRequest`");
		} else {
			binlogSubscriptionRequest.setDatabase(params.get("database").get(0));
		}

		if (!params.containsKey("table")) {
			throw new DecoderException("must contain `table` in `BinlogSubscriptionRequest`");
		} else {
			binlogSubscriptionRequest.setTables(params.get("table"));
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
