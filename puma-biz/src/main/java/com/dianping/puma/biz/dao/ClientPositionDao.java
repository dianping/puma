package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientPositionEntity;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionDao {
    ClientPositionEntity findByClientName(String clientName);

    int update(ClientPositionEntity entity);

    int insert(ClientPositionEntity entity);
}
