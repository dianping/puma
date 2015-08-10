package com.dianping.puma.instance;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.zebra.Constants;
import com.dianping.zebra.config.LionConfigService;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

public class ZebraInstanceManager extends AbstractInstanceManager {

    @Autowired
    private LionConfigService lionService;

    @Autowired
    private PumaTaskTargetDao pumaTaskTargetDao;

    @Autowired
    private PumaServerService pumaServerService;

    private final String env = EnvZooKeeperConfig.getEnv();

    @Override
    public void init() {
    }

    protected void buildConfigFromZebra() throws IOException {
        Map<String, Set<String>> targets = getTargets();

        Map<String, InstanceChangedEvent> cachedEvent = new HashMap<String, InstanceChangedEvent>();

        /*
        Map<String, String> allProperties = lionService.getConfigByProject(env, Constants.DEFAULT_DATASOURCE_GROUP_PRFIX);
        for (String groupds : allProperties.values()) {
            Map<String, DefaultDataSourceConfigManager.ReadOrWriteRole> groupdsResult = DefaultDataSourceConfigManager.ReadOrWriteRole.parseConfig(groupds);
            for (Map.Entry<String, DefaultDataSourceConfigManager.ReadOrWriteRole> entry : groupdsResult.entrySet()) {

            }
        }*/
    }

    protected Map<String, Set<String>> getTargets() {
        List<PumaServerEntity> servers = pumaServerService.findOnCurrentServer();
        Map<String, Set<String>> targetResult = new HashMap<String, Set<String>>();
        for (PumaServerEntity server : servers) {
            List<PumaTaskTargetEntity> targets = pumaTaskTargetDao.findByTaskId(server.getId());
            for (PumaTaskTargetEntity target : targets) {
                Set<String> tables = targetResult.get(target.getDatabase());
                if (tables == null) {
                    tables = new HashSet<String>();
                    targetResult.put(target.getDatabase(), tables);
                }
                tables.add(target.getTable());
            }
        }
        return targetResult;
    }
}