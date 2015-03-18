package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SyncServerDao;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.entity.morphia.SyncServerMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("syncServerDao")
public class SyncServerMorphiaDao extends BasicDAO<SyncServerMorphiaEntity, String> implements SyncServerDao{

	@Autowired
	public SyncServerMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public SyncServer find(String id) {
		Query<SyncServerMorphiaEntity> q = this.getDatastore().createQuery(SyncServerMorphiaEntity.class);
		q.field("id").equal(id);
		SyncServerMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	public SyncServer findByHost(String host,int port){
		Query<SyncServerMorphiaEntity> q = this.getDatastore().createQuery(SyncServerMorphiaEntity.class).disableValidation();
		q.field("entity.host").equal(host);
		q.field("entity.port").equal(port);
		SyncServerMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}
	
	public List<SyncServer> findAll() {
		Query<SyncServerMorphiaEntity> q = this.getDatastore().createQuery(SyncServerMorphiaEntity.class);
		QueryResults<SyncServerMorphiaEntity> result = this.find(q);
		List<SyncServerMorphiaEntity> morphiaEntities = result.asList();

		List<SyncServer> entities = new ArrayList<SyncServer>();
		for (SyncServerMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	public void create(SyncServer entity) {
		SyncServerMorphiaEntity morphiaEntity = new SyncServerMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	public void update(SyncServer entity) {
		this.create(entity);
	}

	public void remove(String id) {
		Query<SyncServerMorphiaEntity> q = this.getDatastore().createQuery(SyncServerMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
