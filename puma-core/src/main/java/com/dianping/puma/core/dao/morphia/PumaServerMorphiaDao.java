package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.PumaServerDao;
import com.dianping.puma.core.entity.PumaServerEntity;
import com.dianping.puma.core.entity.morphia.PumaServerMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("pumaServerDao")
public class PumaServerMorphiaDao extends BasicDAO<PumaServerMorphiaEntity, String> implements PumaServerDao {

	@Autowired
	public PumaServerMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public PumaServerEntity find(String id) {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		q.field("id").equal(id);
		PumaServerMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<PumaServerEntity> findAll() {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		QueryResults<PumaServerMorphiaEntity> result = this.find(q);
		List<PumaServerMorphiaEntity> morphiaEntities = result.asList();

		List<PumaServerEntity> entities = new ArrayList<PumaServerEntity>();
		for (PumaServerMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(PumaServerEntity entity) {
		PumaServerMorphiaEntity morphiaEntity = new PumaServerMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaServerEntity entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
