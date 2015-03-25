package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SyncServerDao;
import com.dianping.puma.core.dao.morphia.helper.MongoClient;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.entity.morphia.SyncServerMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("syncServerDao")
public class SyncServerMorphiaDao extends MongoBaseDao<SyncServerMorphia> implements SyncServerDao{

	@Autowired
	public SyncServerMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public SyncServer find(String name) {
		Query<SyncServerMorphia> q = this.getDatastore().createQuery(SyncServerMorphia.class);
		q.field("name").equal(name);
		SyncServerMorphia syncServerMorphia = this.findOne(q);
		return (syncServerMorphia == null) ? null : syncServerMorphia.getEntity();
	}

	public SyncServer findByHost(String host,int port){
		Query<SyncServerMorphia> q = this.getDatastore().createQuery(SyncServerMorphia.class).disableValidation();
		q.field("entity.host").equal(host);
		q.field("entity.port").equal(port);
		SyncServerMorphia syncServerMorphia = this.findOne(q);
		return (syncServerMorphia == null) ? null : syncServerMorphia.getEntity();
	}
	
	public List<SyncServer> findAll() {
		Query<SyncServerMorphia> q = this.getDatastore().createQuery(SyncServerMorphia.class);
		QueryResults<SyncServerMorphia> result = this.find(q);
		List<SyncServerMorphia> syncServerMorphias = result.asList();

		List<SyncServer> syncServers = new ArrayList<SyncServer>();
		for (SyncServerMorphia syncServerMorphia: syncServerMorphias) {
			syncServers.add(syncServerMorphia.getEntity());
		}
		return syncServers;
	}

	public void create(SyncServer syncServer) {
		SyncServerMorphia syncServerMorphia = new SyncServerMorphia(syncServer);
		this.save(syncServerMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(SyncServer syncServer) {
		SyncServerMorphia syncServerMorphia = new SyncServerMorphia(syncServer);
		Query<SyncServerMorphia> q = this.getDatastore().createQuery(SyncServerMorphia.class);
		q.field("name").equal(syncServerMorphia.getName());
		UpdateOperations<SyncServerMorphia> uop = this.getDatastore().createUpdateOperations(SyncServerMorphia.class);
		uop.set("entity", syncServer);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<SyncServerMorphia> q = this.getDatastore().createQuery(SyncServerMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
}
