package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.SrcDBInstanceDao;
import com.dianping.puma.biz.entity.SrcDBInstance;
import com.dianping.puma.biz.service.SrcDBInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBInstanceService")
public class SrcDBInstanceServiceImpl implements SrcDBInstanceService {
	@Autowired
	SrcDBInstanceDao srcDbInstanceDao;

	@Override
	public SrcDBInstance find(String name) {
		return srcDbInstanceDao.find(name);
	}

	@Override
	public SrcDBInstance find(long id) {
		return srcDbInstanceDao.find(id);
	}

	@Override
	public List<SrcDBInstance> findAll() {
		return srcDbInstanceDao.findAll();
	}

	@Override
	public long count() {
		return srcDbInstanceDao.count();
	}
	
	@Override
	public List<SrcDBInstance> findByPage(int page, int pageSize) {
		return srcDbInstanceDao.findByPage(page, pageSize);
	}
	
	@Override
	public void create(SrcDBInstance srcDBInstance) {
		srcDbInstanceDao.create(srcDBInstance);
	}

	@Override
	public void update(SrcDBInstance srcDBInstance) {
		srcDbInstanceDao.update(srcDBInstance);
	}

	@Override
	public void remove(String name) {
		srcDbInstanceDao.remove(name);
	}
	
	@Override
	public void remove(long id) {
		srcDbInstanceDao.remove(id);
	}

    @Override
    public List<SrcDBInstance> findByIp(String ip) {
        return srcDbInstanceDao.findByIp(ip);
    }
}
