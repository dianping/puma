package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PumaServerDao {

    PumaServerEntity findById(int id);

    List<PumaServerEntity> findByIds(@Param("list") List<Integer> ids);

    PumaServerEntity findByName(String name);

    PumaServerEntity findByHost(String host);

    List<PumaServerEntity> findAll();

    List<PumaServerEntity> findAllAlive();

    List<PumaServerEntity> findByPage(@Param(value = "offset") int offset, @Param(value = "limit") int limit);

    long count();

    int insert(PumaServerEntity entity);

    int update(PumaServerEntity entity);

    int delete(int id);

    int deleteByName(String name);
}
