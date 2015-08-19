package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaTargetService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PumaTargetServiceImpl implements PumaTargetService {

	@Autowired
	PumaTargetDao pumaTargetDao;

	@Autowired
	PumaServerTargetDao pumaServerTargetDao;

	@Autowired
	PumaServerDao pumaServerDao;

	@Override
	public PumaTargetEntity findById(int id) {
		PumaTargetEntity entity = pumaTargetDao.findById(id);
		splitTables(entity);
		return entity;
	}

	@Override
	public PumaTargetEntity findByDatabase(String database) {
		PumaTargetEntity entity = pumaTargetDao.findByDatabase(database);
		splitTables(entity);
		return entity;
	}

	@Override
	public List<PumaTargetEntity> findByHost(String host) {
		PumaServerEntity pumaServer = pumaServerDao.findByHost(host);
		List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetDao.findByServerId(pumaServer.getId());
		return Lists.transform(pumaServerTargets, new Function<PumaServerTargetEntity, PumaTargetEntity>() {
			@Override
			public PumaTargetEntity apply(PumaServerTargetEntity pumaServerTarget) {
				int targetId = pumaServerTarget.getTargetId();
				PumaTargetEntity pumaTarget = findById(targetId);
				pumaTarget.setBeginTime(pumaServerTarget.getBeginTime());
				return pumaTarget;
			}
		});
	}

	@Override
	public int create(PumaTargetEntity entity) {
		mergeTables(entity);
		return pumaTargetDao.insert(entity);
	}

	@Override
	public int update(PumaTargetEntity entity) {
		mergeTables(entity);
		return pumaTargetDao.update(entity);
	}

	@Override public int remove(int id) {
		return pumaTargetDao.delete(id);
	}

	protected void mergeTables(PumaTargetEntity entity) {
		if (entity != null) {
			List<String> tableList = entity.getTables();
			String tables = StringUtils.join(tableList, ",");
			entity.setFormatTables(tables);
		}
	}

	protected void splitTables(PumaTargetEntity entity) {
		if (entity != null) {
			String tables = entity.getFormatTables();
			String[] tableArray = StringUtils.split(tables, ",");
			List<String> tableList = Arrays.asList(tableArray);
			entity.setTables(tableList);
		}
	}
}
