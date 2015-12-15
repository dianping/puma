package com.dianping.puma.server.registry;

import java.util.List;
import java.util.Set;

public interface RegistryService {

    List<String> find(String database);

    Set<String> findAllDatabase();

    void register(String host, String database);

    void unregister(String host, String database);

    void registerAll(List<String> hosts, String database);

    void unregisterAll(List<String> hosts, String database);

    void registerResetAll(List<String> hosts, String database);
}