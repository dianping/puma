package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.old.DumpTask;
import org.springframework.stereotype.Service;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Service
public class DumpTaskStateServiceImpl extends TaskStateServiceImpl {
    @Override
    protected String getTypeName() {
        return DumpTask.class.getName();
    }
}
