package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.PumaTaskDao;
import com.dianping.puma.core.entity.PumaTaskEntity;
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

	public PumaTaskEntity find(String id) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		q.field("id").equal(id);
		PumaTaskMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<PumaTaskEntity> findAll() {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		QueryResults<PumaTaskMorphiaEntity> result = this.find(q);
		List<PumaTaskMorphiaEntity> morphiaEntities = result.asList();

		List<PumaTaskEntity> entities = new ArrayList<PumaTaskEntity>();
		for (PumaTaskMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(PumaTaskEntity entity) {
		PumaTaskMorphiaEntity morphiaEntity = new PumaTaskMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaTaskEntity entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<PumaTaskMorphiaEntity> q = this.getDatastore().createQuery(PumaTaskMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
