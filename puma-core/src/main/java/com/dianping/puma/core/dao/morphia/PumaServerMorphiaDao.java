package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.PumaServerDao;
import com.dianping.puma.core.entity.PumaServer;
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

	public PumaServer find(String id) {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		q.field("id").equal(id);
		PumaServerMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public PumaServer findByHostAndPort(String host, Integer port) {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class).disableValidation();
		q.field("entity.host").equal(host);
		q.field("entity.port").equal(port);
		PumaServerMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public List<PumaServer> findAll() {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		QueryResults<PumaServerMorphiaEntity> result = this.find(q);
		List<PumaServerMorphiaEntity> morphiaEntities = result.asList();

		List<PumaServer> entities = new ArrayList<PumaServer>();
		for (PumaServerMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(PumaServer entity) {
		PumaServerMorphiaEntity morphiaEntity = new PumaServerMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaServer entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<PumaServerMorphiaEntity> q = this.getDatastore().createQuery(PumaServerMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
