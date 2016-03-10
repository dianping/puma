package com.dianping.puma.biz.service;

import com.dianping.puma.common.model.Client;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientService {

    List<Client> findAll();

    void create(Client client);
}
