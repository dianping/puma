package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogQuery;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class BinlogQueryDecoder implements RequestDecoder {

	private static final boolean   DEFAULT_AUTO_ACK    = false;
	private static final int       DEFAULT_BATCH_SIZE  = 1;
	private static final long      DEFAULT_TIMEOUT     = 0;
	private static final TimeUnit  DEFAULT_TIME_UNIT   = TimeUnit.MILLISECONDS;

	Pattern pattern = Pattern.compile("^/puma/binlog/query.*$");

	@Override
	public boolean match(FullHttpRequest request) {
		return pattern.matcher(request.getUri()).matches();
	}

	@Override
	public Object decode(FullHttpRequest request) {
		BinlogQuery binlogQuery = new BinlogQuery();
		Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

		if (params.containsKey("clientName")) {
			binlogQuery.setClientName(params.get("clientName").get(0));
		} else {
			throw new RuntimeException("no client name given in binlog query.");
		}

		binlogQuery.setAutoAck(
				params.containsKey("autoAck") ? Boolean.valueOf(params.get("autoAck").get(0)) : DEFAULT_AUTO_ACK
		);

		binlogQuery.setBatchSize(
				params.containsKey("batchSize") ? Integer.valueOf(params.get("batchSize").get(0)) : DEFAULT_BATCH_SIZE
		);

		binlogQuery.setTimeout(
				params.containsKey("timeout") ? Long.valueOf(params.get("timeout").get(0)) : DEFAULT_TIMEOUT
		);

		binlogQuery.setTimeUnit(
				params.containsKey("timeUnit") ? TimeUnit.valueOf(params.get("timeUnit").get(0)) : DEFAULT_TIME_UNIT
		);

		return binlogQuery;
	}
}
