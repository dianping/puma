package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientTokenEntity;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientTokenDao {

    ClientTokenEntity find(String clientName);

    void insert(ClientTokenEntity entity);

    int update(ClientTokenEntity entity);

    int delete(String clientName);
}
