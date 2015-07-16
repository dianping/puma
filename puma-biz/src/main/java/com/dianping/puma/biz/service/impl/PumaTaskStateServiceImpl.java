package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.biz.service.PumaTaskStateService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class PumaTaskStateServiceImpl implements PumaTaskStateService {

    @Override
    public List<PumaTaskStateEntity> find(String name) {
        return null;
    }

    @Override
    public PumaTaskStateEntity find(String name, String serverName) {
        return null;
    }

    @Override
    public List<PumaTaskStateEntity> findByServerName(String serverName) {
        return null;
    }

    @Override
    public void createOrUpdate(PumaTaskStateEntity state) {
        state.setGmtUpdate(new Date());
    }
}
