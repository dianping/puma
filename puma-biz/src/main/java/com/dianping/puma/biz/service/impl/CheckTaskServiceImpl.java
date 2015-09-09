package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.CheckTaskColumnMappingDao;
import com.dianping.puma.biz.dao.CheckTaskDao;
import com.dianping.puma.biz.entity.CheckTaskColumnMappingEntity;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.biz.service.CheckTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckTaskServiceImpl implements CheckTaskService {

	@Autowired
	CheckTaskDao checkTaskDao;

	@Autowired
	CheckTaskColumnMappingDao checkTaskColumnMappingDao;

	@Override
	public CheckTaskEntity findById(int id) {
		CheckTaskEntity checkTaskEntity = checkTaskDao.findById(id);

		if (checkTaskEntity == null) {
			return null;
		}

		loadColumnMapping(checkTaskEntity);
		return checkTaskEntity;
	}

	@Override
	public List<CheckTaskEntity> findAll() {
		List<CheckTaskEntity> checkTaskEntities = checkTaskDao.findAll();

		for (CheckTaskEntity checkTaskEntity : checkTaskEntities) {
			loadColumnMapping(checkTaskEntity);
		}

		return checkTaskEntities;
	}

	protected void loadColumnMapping(CheckTaskEntity checkTaskEntity) {
		Map<String, String> columnMapping = new HashMap<String, String>();

		int id = checkTaskEntity.getId();
		List<CheckTaskColumnMappingEntity> checkTaskColumnMappingEntities = checkTaskColumnMappingDao.findByTaskId(id);
		for (CheckTaskColumnMappingEntity checkTaskColumnMappingEntity : checkTaskColumnMappingEntities) {
			String srcColumn = checkTaskColumnMappingEntity.getSrcColumn();
			String dstColumn = checkTaskColumnMappingEntity.getDstColumn();
			columnMapping.put(srcColumn, dstColumn);
		}

		checkTaskEntity.setColumnMapping(columnMapping);
	}
}
