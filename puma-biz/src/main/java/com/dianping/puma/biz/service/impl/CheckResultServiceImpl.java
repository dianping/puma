package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.CheckResultDao;
import com.dianping.puma.biz.entity.CheckResultEntity;
import com.dianping.puma.biz.service.CheckResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class CheckResultServiceImpl implements CheckResultService {
    @Autowired
    private CheckResultDao checkResultDao;

    @Override
    public int create(CheckResultEntity entity) {
        return checkResultDao.create(entity);
    }
}
