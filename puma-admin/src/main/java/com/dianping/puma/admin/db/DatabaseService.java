package com.dianping.puma.admin.db;

import java.util.List;

public interface DatabaseService {

	public String findWriteJdbcUrl(String database);

	public List<String> findAll();
}
