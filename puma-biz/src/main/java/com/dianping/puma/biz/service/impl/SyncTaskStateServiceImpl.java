package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.old.SyncTask;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SyncTaskStateServiceImpl extends TaskStateServiceImpl {
    @Override
    protected String getTypeName() {
        return SyncTask.class.getName();
    }
}
