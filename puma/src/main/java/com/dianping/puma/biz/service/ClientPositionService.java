package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.ClientPositionEntity;

import java.util.List;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionService {

    List<ClientPositionEntity> findAll();

    ClientPositionEntity find(String clientName);

    void update(ClientPositionEntity clientPositionEntity, boolean flush);

    void flush();

    void cleanUpTestClients();
}
