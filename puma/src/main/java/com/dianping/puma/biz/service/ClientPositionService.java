package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.ClientPositionEntity;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionService {
    ClientPositionEntity find(String clientName);

    void update(ClientPositionEntity clientPositionEntity, boolean flush);

    void flush();
}
