package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaTargetEntity;

import java.util.List;

public interface PumaTaskTargetService {
    List<PumaTargetEntity> findTargetByServerName(String pumaServerName);
}
