package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface SrcDbDao {

    SrcDbEntity findById(int id);

    List<SrcDbEntity> findByIds(List<Integer> id);

    SrcDbEntity findByName(String name);

    List<SrcDbEntity> findAll();

    List<SrcDbEntity> findByPage(int offset, int limit);

    long count();

    int insert(SrcDbEntity entity);

    int update(SrcDbEntity entity);

    int delete(int id);

    int deleteByName(String name);
}
