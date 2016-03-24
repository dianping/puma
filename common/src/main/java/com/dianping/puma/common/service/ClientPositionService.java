package com.dianping.puma.common.service;

import com.dianping.puma.common.entity.ClientPositionEntity;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionService {
    ClientPositionEntity find(String clientName);

    void update(ClientPositionEntity clientPositionEntity);

    void flush();

    void cleanUpTestClients();
}
