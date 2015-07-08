package com.dianping.puma.task;

import com.dianping.puma.biz.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dozer @ 7/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component
public class HeartbeatTask {

    @Autowired
    private PumaServerService pumaServerService;

    @Scheduled(fixedDelay = 60 * 1000)
    public void headtbeat() {
        pumaServerService.heartBeat();
    }
}
