package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.config.MysqlConfig;

public interface MysqlConfigService {

    /**
     * 如果id为空或id没有匹配，则是创建；id不为空且有匹配，则是更新(当然也可以使用UpdateOperations)
     */
    ObjectId saveMysqlConfig(MysqlConfig MysqlConfig);

    void modifyMysqlConfig(MysqlConfig MysqlConfig);

    /**
     * 删除相应的MysqlConfig和SyncXml
     */
    void removeMysqlConfig(ObjectId id);

    List<MysqlConfig> findMysqlConfigs();

    MysqlConfig findMysqlConfig(ObjectId objectId);

    MysqlConfig findMysqlConfig(String name);

}
