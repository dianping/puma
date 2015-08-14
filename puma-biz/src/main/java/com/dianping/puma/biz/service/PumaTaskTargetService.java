package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaTaskTargetEntity;

import java.util.List;

public interface PumaTaskTargetService {
    List<PumaTaskTargetEntity> findTargetByServerName(String pumaServerName);
}
