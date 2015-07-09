package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.TaskStateEntity;

import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TaskStateDao {
    public List<TaskStateEntity> find();
}
