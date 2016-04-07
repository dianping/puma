package com.dianping.puma.portal.service.impl;

import com.dianping.cat.Cat;
import com.dianping.puma.common.model.PumaServer;
import com.dianping.puma.common.model.PumaServerTarget;
import com.dianping.puma.common.service.PumaServerService;
import com.dianping.puma.common.service.PumaServerTargetService;
import com.dianping.puma.portal.service.PumaRouterCleanUpService;
import com.dianping.puma.portal.service.RegistryService;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class PumaRouterCleanUpServiceImpl implements PumaRouterCleanUpService {

    @Autowired
    PumaServerService pumaServerService;

    @Autowired
    PumaServerTargetService pumaServerTargetService;

    @Autowired
    RegistryService registryService;

    @Override
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void cleanUp() {
        try {
            if (new Random().nextInt(24) != 1) {
                return;
            }

            Cat.logEvent("Scheduled", "RouterCleanUp");
            Set<String> allDb = registryService.findAllDatabase();
            for (final String db : allDb) {
                List<String> needToUnregistry = new ArrayList<String>();
                List<String> servers = registryService.find(db);
                for (String server : servers) {
                    if (!server.contains(":")) {
                        needToUnregistry.add(server);
                        continue;
                    }

                    String serverIP = StringUtils.substringBefore(server, ":");
                    PumaServer pumaServer = pumaServerService.findByHost(serverIP);
                    if (pumaServer == null || !pumaServer.checkAlive()) {
                        needToUnregistry.add(server);
                        continue;
                    }

                    List<PumaServerTarget> pumaServerTargets = pumaServerTargetService.findByServerHost(pumaServer.getHost());
                    boolean anyMatches = FluentIterable.from(pumaServerTargets).anyMatch(new Predicate<PumaServerTarget>() {
                        @Override
                        public boolean apply(PumaServerTarget pumaServerTarget) {
                            return db.equals(pumaServerTarget.getTargetDb());
                        }
                    });

                    if (!anyMatches) {
                        needToUnregistry.add(server);
                    }
                }

                if (needToUnregistry.size() > 0) {
                    registryService.unregisterAll(needToUnregistry, db);
                }
            }
        } catch (Exception e) {
            Cat.logError("PumaRouterCleanUpServiceImpl Failed", e);
        }
    }
}