package com.dianping.puma.server.extension.registry;

import com.dianping.puma.core.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LionRegistryService implements RegistryService {

	private static final String LION_CREATE_URL = "http://lionapi.dp:8080/config2/create?id=%s&project=%s&key=%s&desc=%s";
	private static final String LION_SET_URL = "http://lionapi.dp:8080/config2/set?env=%s&id=%s&key=%s&value=%s";

	@Autowired
	ConfigManager configManager;

	@Override
	public void register(String host, String database, List<String> tables) {

	}

	@Override
	public void unregister(String host, String database, List<String> tables) {

	}
}
