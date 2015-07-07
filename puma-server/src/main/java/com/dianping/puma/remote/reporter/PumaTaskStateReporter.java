package com.dianping.puma.remote.reporter;

import com.dianping.puma.biz.service.PumaTaskStateService;
import com.dianping.puma.config.PumaServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("pumaTaskStateReporter")
public class PumaTaskStateReporter {

    @Autowired
    PumaServerConfig pumaServerConfig;

    @Autowired
    PumaTaskStateService pumaTaskStateService;

    @Scheduled(cron = "0/5 * * * * ?")
    public void report() {
//		PumaTaskStateEvent event = new PumaTaskStateEvent();
//		event.setServerName(pumaServerConfig.getName());
//		event.setTaskStates(pumaTaskStateService.findAll());
//		pumaTaskStatePublisher.publish(event);
    }
}
