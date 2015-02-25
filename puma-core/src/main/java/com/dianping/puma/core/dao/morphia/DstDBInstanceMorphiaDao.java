package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.DstDBInstanceDao;
import com.dianping.puma.core.entity.DstDBInstanceEntity;
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
	public DstDBInstanceMorphiaDao(MongoClient mongoClient2) {
		super(mongoClient2.getDatastore());
	}

	public DstDBInstanceEntity find(String id) {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		DstDBInstanceMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<DstDBInstanceEntity> findAll() {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		QueryResults<DstDBInstanceMorphiaEntity> result = this.find(q);
		List<DstDBInstanceMorphiaEntity> morphiaEntities = result.asList();

		List<DstDBInstanceEntity> entities = new ArrayList<DstDBInstanceEntity>();
		for(DstDBInstanceMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(DstDBInstanceEntity entity) {
		DstDBInstanceMorphiaEntity morphiaEntity = new DstDBInstanceMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(DstDBInstanceEntity entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<DstDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(DstDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
