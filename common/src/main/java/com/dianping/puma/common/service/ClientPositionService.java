package com.dianping.puma.common.service;

import com.dianping.puma.common.model.ClientPosition;

import java.util.List;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface ClientPositionService {

    List<ClientPosition> findAll();

    ClientPosition find(String clientName);

    void update(ClientPosition clientPosition);

    void flush();

    void cleanUpTestClients();
}
