package com.dianping.puma.portal.service.impl;

import com.dianping.puma.common.service.ClientPositionService;
import com.dianping.puma.portal.service.PumaClientCleanUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class PumaClientCleanUpServiceImpl implements PumaClientCleanUpService {

    @Autowired
    private ClientPositionService clientPositionService;

    @Override
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void cleanup() {
        clientPositionService.cleanUpTestClients();
    }
}
