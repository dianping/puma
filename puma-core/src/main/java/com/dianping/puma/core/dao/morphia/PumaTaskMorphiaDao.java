package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.PumaTaskDao;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.morphia.PumaTaskMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("pumaTaskDao")
public class PumaTaskMorphiaDao extends BasicDAO<PumaTaskMorphiaEntity, String> implements PumaTaskDao {

	@Autowired
	public PumaTaskMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public PumaTask find(String id) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		q.field("id").equal(id);
		PumaTaskMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<PumaTask> findBySrcDBInstanceId(String srcDBInstanceId) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class).disableValidation();
		q.field("entity.srcDBInstanceId").equal(srcDBInstanceId);
		QueryResults<PumaTaskMorphiaEntity> result = this.find(q);
		List<PumaTaskMorphiaEntity> morphiaEntities = result.asList();

		List<PumaTask> entities = new ArrayList<PumaTask>();
		for (PumaTaskMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public List<PumaTask> findByPumaServerId(String pumaServerName) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class).disableValidation();
		q.field("entity.pumaServerId").equal(pumaServerName);
		QueryResults<PumaTaskMorphiaEntity> result = this.find(q);
		List<PumaTaskMorphiaEntity> morphiaEntities = result.asList();

		List<PumaTask> entities = new ArrayList<PumaTask>();
		for (PumaTaskMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public List<PumaTask> findAll() {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		QueryResults<PumaTaskMorphiaEntity> result = this.find(q);
		List<PumaTaskMorphiaEntity> morphiaEntities = result.asList();

		List<PumaTask> entities = new ArrayList<PumaTask>();
		for (PumaTaskMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(PumaTask entity) {
		PumaTaskMorphiaEntity morphiaEntity = new PumaTaskMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaTask entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
