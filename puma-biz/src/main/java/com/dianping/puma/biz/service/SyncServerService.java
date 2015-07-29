package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SyncServerEntity;
import com.dianping.puma.biz.entity.old.SyncServer;

import java.util.List;

public interface SyncServerService {

    SyncServerEntity findById(int id);

    SyncServerEntity findByName(String name);

    List<SyncServer> findAll();

    int create(SyncServer syncServer);

    int update(SyncServer syncServer);

    int remove(int id);

    int remove(String name);
}
