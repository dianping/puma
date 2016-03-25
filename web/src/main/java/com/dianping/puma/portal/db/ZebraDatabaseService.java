package com.dianping.puma.portal.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZebraDatabaseService implements DatabaseService {

	@Autowired
	InstanceManager instanceManager;

	@Override
	public String findWriteJdbcUrl(String database) {
		String host = instanceManager.getDbClusterMap().get(database);
		if (host == null) {
			return null;
		} else {
			return "jdbc:mysql://" + host + "/" + database + "?socketTimeout=60000";
		}
	}

	@Override
	public List<String> findAll() {
		List<String> databases = new ArrayList<String>();
		databases.addAll(instanceManager.getDbClusterMap().keySet());
		return databases;
	}
}
