package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PumaServerTargetServiceImpl implements PumaServerTargetService {

	@Autowired
	PumaServerTargetDao pumaServerTargetDao;

	@Override
	public PumaServerTargetEntity findById(int id) {
		return pumaServerTargetDao.findById(id);
	}

	@Override
	public List<PumaServerTargetEntity> findByTargetId(int targetId) {
		return pumaServerTargetDao.findByTargetId(targetId);
	}

	@Override
	public List<PumaServerTargetEntity> findByDatabase(String database) {
		return Lists.transform(pumaServerTargetDao.findByDatabase(database),
				new Function<PumaServerTargetEntity, PumaServerTargetEntity>() {
					@Override
					public PumaServerTargetEntity apply(PumaServerTargetEntity entity) {
						splitTables(entity);
						return entity;
					}
				});
	}

	@Override
	public List<PumaServerTargetEntity> findByServerId(int serverId) {
		return pumaServerTargetDao.findByServerId(serverId);
	}

	@Override
	public List<PumaServerTargetEntity> findByHost(String host) {
		return Lists.transform(pumaServerTargetDao.findByHost(host),
				new Function<PumaServerTargetEntity, PumaServerTargetEntity>() {
					@Override
					public PumaServerTargetEntity apply(PumaServerTargetEntity entity) {
						splitTables(entity);
						return entity;
					}
				});
	}

	@Override
	public int createOrUpdate(PumaServerTargetEntity entity) {
		List<PumaServerTargetEntity> pumaServerTargets = findByTargetId(entity.getTargetId());
		for (PumaServerTargetEntity pumaServerTarget : pumaServerTargets) {
			if (entity.getServerId() == pumaServerTarget.getServerId()) {
				return update(entity);
			}
		}
		return create(entity);
	}

	@Override
	public int create(PumaServerTargetEntity entity) {
		return pumaServerTargetDao.insert(entity);
	}

	@Override
	public int update(PumaServerTargetEntity entity) {
		return pumaServerTargetDao.update(entity);
	}

	@Override
	public int remove(int id) {
		return pumaServerTargetDao.delete(id);
	}

	protected void splitTables(PumaServerTargetEntity entity) {
		if (entity != null) {
			String tables = entity.getFormatTables();
			String[] tableArray = StringUtils.split(tables, "+");
			List<String> tableList = Arrays.asList(tableArray);
			entity.setTables(tableList);
		}
	}
}
