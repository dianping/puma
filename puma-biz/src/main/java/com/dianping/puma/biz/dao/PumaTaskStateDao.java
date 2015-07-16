package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskStateDao {

	PumaTaskStateEntity find(int id);

	List<PumaTaskStateEntity> findByTaskName(String taskName);

	PumaTaskStateEntity findByTaskNameAndServerName(
			@Param("taskName") String taskName,
			@Param("serverName") String serverName
	);

	int insert(PumaTaskStateEntity entity);

	int update(PumaTaskStateEntity entity);
}
