package com.dianping.puma.pumaserver.router.decoder;

import com.dianping.puma.core.netty.entity.BinlogSubscription;
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
	public Object decode(FullHttpRequest request) {
		BinlogSubscription binlogSubscription = new BinlogSubscription();
		Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();

		List<String> clientName = params.get("clientName");
		if (clientName == null || clientName.size() == 0) {
			throw new RuntimeException("must contain client name in binlog subscription.");
		}
		binlogSubscription.setClientName(clientName.get(0));

		List<String> database = params.get("database");
		if (database == null || database.size() == 0) {
			throw new RuntimeException("must contain database in binlog subscription.");
		}
		binlogSubscription.setDatabase(database.get(0));

		List<String> tables = params.get("table");
		if (tables == null || tables.size() == 0) {
			throw new RuntimeException("must contain at least one table in binlog subscription");
		}
		binlogSubscription.setTables(tables);

		List<String> ddlStr = params.get("ddl");
		binlogSubscription.setDdl(
				(ddlStr == null || ddlStr.size() == 0) ? DEFAULT_DDL : Boolean.valueOf(ddlStr.get(0))
		);

		List<String> dmlStr = params.get("dml");
		binlogSubscription.setDml(
				(dmlStr == null || dmlStr.size() == 0) ? DEFAULT_DML : Boolean.valueOf(dmlStr.get(0))
		);

		List<String> transactionStr = params.get("transaction");
		binlogSubscription.setTransaction(
				(transactionStr == null || transactionStr.size() == 0) ? DEFAULT_TRANSACTION : Boolean.valueOf(transactionStr.get(0))
		);

		return binlogSubscription;
	}
}
