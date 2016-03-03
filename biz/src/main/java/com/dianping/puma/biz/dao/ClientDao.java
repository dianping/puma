package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientEntity;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientDao {

    List<ClientEntity> findAll();

    void insert(ClientEntity entity);
}
