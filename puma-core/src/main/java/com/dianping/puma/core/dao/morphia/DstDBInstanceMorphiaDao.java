package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.DstDBInstanceDao;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.morphia.DstDBInstanceMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("dstDBInstanceDao")
public class DstDBInstanceMorphiaDao extends BasicDAO<DstDBInstanceMorphiaEntity, String>
		implements DstDBInstanceDao {

	@Autowired
	public DstDBInstanceMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public DstDBInstance find(String id) {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		DstDBInstanceMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<DstDBInstance> findAll() {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		QueryResults<DstDBInstanceMorphiaEntity> result = this.find(q);
		List<DstDBInstanceMorphiaEntity> morphiaEntities = result.asList();

		List<DstDBInstance> entities = new ArrayList<DstDBInstance>();
		for(DstDBInstanceMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(DstDBInstance entity) {
		DstDBInstanceMorphiaEntity morphiaEntity = new DstDBInstanceMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(DstDBInstance entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
