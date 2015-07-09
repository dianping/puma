package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.old.PumaServer;
import org.apache.ibatis.annotations.Param;

public interface PumaServerDao {

	PumaServer findByName(@Param("name") String name);
}
