package com.dianping.puma.admin.service;

import com.dianping.puma.admin.model.PumaServerStatusDto;

import java.util.Map;

/**
 * Dozer @ 15/8/24
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskStatusService {
    Map<String, PumaServerStatusDto> getAllStatus();

    PumaServerStatusDto getStatusByName(String name);
}
