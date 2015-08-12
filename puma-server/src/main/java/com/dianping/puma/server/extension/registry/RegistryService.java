package com.dianping.puma.server.extension.registry;

import java.util.List;

public interface RegistryService {

	public void register(String host, String database, List<String> tables);

	public void unregister(String host, String database, List<String> tables);
}
