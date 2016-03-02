package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientConfigEntity;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientConfigDao {

    void insert(ClientConfigEntity entity);

    int update(ClientConfigEntity entity);

    int delete(String clientName);
}
