package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.config.PumaServerConfig;

public interface PumaServerConfigService {

    /**
     * 如果id为空或id没有匹配，则是创建；id不为空且有匹配，则是更新(当然也可以使用UpdateOperations)
     */
    ObjectId save(PumaServerConfig pumaServerConfig);

    void modify(PumaServerConfig pumaServerConfig);

    void remove(ObjectId id);

    List<PumaServerConfig> findAll();

    PumaServerConfig find(ObjectId objectId);

    PumaServerConfig find(String name);

}
