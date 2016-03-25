package com.dianping.puma.web.service;

import com.dianping.puma.web.model.DashboardModel;
import com.dianping.puma.web.model.PumaServerStatusDto;

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
