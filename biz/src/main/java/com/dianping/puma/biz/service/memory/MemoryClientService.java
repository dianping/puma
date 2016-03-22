package com.dianping.puma.biz.service.memory;

import com.dianping.puma.common.model.Client;
import com.dianping.puma.common.service.PumaClientService;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.MapMaker;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/21.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryClientService implements PumaClientService {

    private ConcurrentMap<String, Client> clientMap = new MapMaker().makeMap();

    @Override
    public Client findByClientName(String clientName) {
        return null;
    }

    @Override
    public List<Client> findAll() {
        return null;
    }

    @Override
    public List<String> findAllClientNames() {
        return FluentIterable.from(clientMap.keySet()).toList();
    }

    @Override
    public void create(Client client) {
        String clientName = client.getClientName();
        clientMap.putIfAbsent(clientName, client);
    }

    @Override
    public void update(Client client) {

    }

    @Override
    public int remove(String clientName) {
        return 0;
    }
}
