package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientPositionEntity;

import java.util.List;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionDao {

    List<ClientPositionEntity> findAll();

    ClientPositionEntity findByClientName(String clientName);

    int update(ClientPositionEntity entity);

    int insert(ClientPositionEntity entity);

    int delete(int id);

    List<ClientPositionEntity> findOldTestClient();
}