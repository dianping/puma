package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerEntity;
import org.apache.ibatis.annotations.Param;

public interface PumaServerDao {

	PumaServerEntity findById(@Param("id") int id);

	void insert(PumaServerEntity entity);

	void delete(int id);
}
