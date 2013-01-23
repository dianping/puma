package com.dianping.puma.syncserver.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;

public interface PumaSyncServerConfigService {

    /**
     * 如果id为空或id没有匹配，则是创建；id不为空且有匹配，则是更新(当然也可以使用UpdateOperations)
     */
    ObjectId save(PumaSyncServerConfig pumaSyncServerConfig);

    void modify(PumaSyncServerConfig pumaSyncServerConfig);

    void remove(ObjectId id);

    List<PumaSyncServerConfig> findAll();

    PumaSyncServerConfig find(ObjectId objectId);

    PumaSyncServerConfig find(String name);

}
