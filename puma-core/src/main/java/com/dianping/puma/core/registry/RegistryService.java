package com.dianping.puma.core.registry;

import java.util.List;

public interface RegistryService {

	public List<String> find(String database);

	public void register(String host, String database);

	public void unregister(String host, String database);

	public void registerAll(List<String> hosts, String database);

	public void unregisterAll(List<String> hosts, String database);

	public void registerResetAll(List<String> hosts, String database);
}
