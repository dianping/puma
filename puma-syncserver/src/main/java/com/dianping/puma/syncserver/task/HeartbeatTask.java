package com.dianping.puma.syncserver.task;

import com.dianping.puma.biz.service.SyncServerService;
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
    private SyncServerService syncServerService;

    @Scheduled(fixedDelay = 60 * 1000)
    public void headtbeat() {
        syncServerService.heartBeat();
    }
}
