package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SrcDbEntity;
import org.apache.ibatis.annotations.Param;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface SrcDbDao {

    SrcDbEntity findById(@Param(value = "id") int id);

    int insert(SrcDbEntity entity);

    int update(SrcDbEntity entity);

}
