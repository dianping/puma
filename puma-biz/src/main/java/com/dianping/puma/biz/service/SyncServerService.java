package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SyncServerEntity;

import java.util.List;

public interface SyncServerService {

    public SyncServerEntity find(int id);

    public List<SyncServerEntity> findAll();

    public int create(SyncServerEntity entity);

    public int update(SyncServerEntity entity);

    public int createOrUpdate(SyncServerEntity entity);

    public int remove(int id);
}
