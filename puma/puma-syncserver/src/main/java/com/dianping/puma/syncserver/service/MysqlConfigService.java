package com.dianping.puma.syncserver.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.config.MysqlConfig;

public interface MysqlConfigService {

    /**
     * 如果id为空或id没有匹配，则是创建；id不为空且有匹配，则是更新(当然也可以使用UpdateOperations)
     */
    ObjectId save(MysqlConfig MysqlConfig);

    void modify(MysqlConfig MysqlConfig);

    /**
     * 删除相应的MysqlConfig
     */
    void remove(ObjectId id);

    List<MysqlConfig> findAll();

    MysqlConfig find(ObjectId objectId);

    MysqlConfig find(String name);

}
