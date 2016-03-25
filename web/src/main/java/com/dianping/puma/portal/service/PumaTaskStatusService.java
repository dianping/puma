package com.dianping.puma.portal.service;

import com.dianping.puma.portal.model.DashboardModel;
import com.dianping.puma.portal.model.PumaServerStatusDto;

import java.util.Map;

/**
 * Dozer @ 15/8/24
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskStatusService {

    Map<String, PumaServerStatusDto> getAllStatus();

    PumaServerStatusDto getStatusByName(String name);

    DashboardModel getDashboard();
}
