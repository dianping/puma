package com.dianping.puma.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.PumaClientInfoDao;
import com.dianping.puma.core.entity.PumaClientInfoEntity;
import com.dianping.puma.service.PumaClientInfoService;

@Service("pumaClientInfoService")
public class PumaClientInfoServiceImpl implements PumaClientInfoService {

	
	@Autowired
	PumaClientInfoDao pumaClientInfoDao;
	
	@Override
	public void create(PumaClientInfoEntity pumaClientInfoEntity) {
		pumaClientInfoDao.create(pumaClientInfoEntity);
	}

}
