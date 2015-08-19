package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaTaskTargetService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PumaTask 会根据 PumaTaskTarget 自动生成
 */
@Service
public class PumaTaskTargetServiceImpl implements PumaTaskTargetService {

    @Autowired
    PumaServerDao pumaServerDao;

    @Autowired
    PumaTaskTargetDao pumaTaskTargetDao;

    @Override
    public List<PumaTargetEntity> findTargetByServerName(String pumaServerName) {
        PumaServerEntity server = pumaServerDao.findByName(pumaServerName);
        if (server == null) {
            return Lists.newArrayList();
        }
        return pumaTaskTargetDao.findByServerId(server.getId());
    }
}
