package com.dianping.puma.web.db;

import java.util.List;

public interface DatabaseService {

	public String findWriteJdbcUrl(String database);

	public List<String> findAll();
}
