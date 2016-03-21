package com.dianping.puma.common.service;

import com.dianping.puma.common.model.Client;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientService {

    Client findByClientName(String clientName);

    List<Client> findAll();

    List<String> findAllClientNames();

    void create(Client client);

    void update(Client client);

    int remove(String clientName);
}
