package com.dianping.puma.core.registry;

import java.util.List;

public interface RegistryService {

	public void register(String host, String database, List<String> tables);

	public void unregister(String host, String database, List<String> tables);
}
