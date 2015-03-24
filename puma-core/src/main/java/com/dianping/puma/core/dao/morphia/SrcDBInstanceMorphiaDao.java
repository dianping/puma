package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.entity.morphia.SrcDBInstanceMorphia;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("srcDBInstanceDao")
public class SrcDBInstanceMorphiaDao extends BasicDAO<SrcDBInstanceMorphia, String>
		implements SrcDBInstanceDao {

	@Autowired
	public SrcDBInstanceMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public SrcDBInstance find(String name) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(name);
		SrcDBInstanceMorphia srcDBInstanceMorphia = this.findOne(q);
		return (srcDBInstanceMorphia == null) ? null : srcDBInstanceMorphia.getEntity();
	}

	public List<SrcDBInstance> findAll() {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		QueryResults<SrcDBInstanceMorphia> result = this.find(q);
		List<SrcDBInstanceMorphia> srcDBInstanceMorphias = result.asList();

		List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
		for(SrcDBInstanceMorphia srcDBInstanceMorphia: srcDBInstanceMorphias) {
			entities.add(srcDBInstanceMorphia.getEntity());
		}
		return entities;
	}

	public void create(SrcDBInstance srcDBInstance) {
		SrcDBInstanceMorphia srcDBInstanceMorphia = new SrcDBInstanceMorphia(srcDBInstance);
		this.save(srcDBInstanceMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(SrcDBInstance srcDBInstance) {
		SrcDBInstanceMorphia srcDBInstanceMorphia = new SrcDBInstanceMorphia(srcDBInstance);
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(srcDBInstance.getName());
		UpdateOperations<SrcDBInstanceMorphia> uop = this.getDatastore().createUpdateOperations(SrcDBInstanceMorphia.class);
		uop.set("entity", srcDBInstance);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
}
