package com.dianping.puma.syncserver.service;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;

public interface PumaServerConfigService {

    PumaServerConfig find(String mysqlName);

}
