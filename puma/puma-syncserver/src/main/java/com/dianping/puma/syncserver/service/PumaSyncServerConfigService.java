package com.dianping.puma.syncserver.service;

import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;

public interface PumaSyncServerConfigService {

    PumaSyncServerConfig find(String host);

}
