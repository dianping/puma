package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.dianping.puma.core.entity.morphia.SrcDBInstanceMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("dbInstanceConfigDao")
public class SrcDBInstanceMorphiaDao extends BasicDAO<SrcDBInstanceMorphiaEntity, String>
		implements SrcDBInstanceDao {

	@Autowired
	public SrcDBInstanceMorphiaDao(MongoClient mongoClient2) {
		super(mongoClient2.getDatastore());
	}

	public SrcDBInstanceEntity find(String id) {
		Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		SrcDBInstanceMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<SrcDBInstanceEntity> findAll() {
		Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
		QueryResults<SrcDBInstanceMorphiaEntity> result = this.find(q);
		List<SrcDBInstanceMorphiaEntity> morphiaEntities = result.asList();

		List<SrcDBInstanceEntity> entities = new ArrayList<SrcDBInstanceEntity>();
		for(SrcDBInstanceMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(SrcDBInstanceEntity entity) {
		SrcDBInstanceMorphiaEntity morphiaEntity = new SrcDBInstanceMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(SrcDBInstanceEntity entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
