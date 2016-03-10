package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientEntity;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientDao {

    ClientEntity find(String clientName);

    List<ClientEntity> findAll();

    void insert(ClientEntity entity);

    int update(ClientEntity entity);

    int delete(String clientName);
}
