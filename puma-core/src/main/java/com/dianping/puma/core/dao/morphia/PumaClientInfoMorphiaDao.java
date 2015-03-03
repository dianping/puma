package com.dianping.puma.core.dao.morphia;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.dao.PumaClientInfoDao;
import com.dianping.puma.core.entity.PumaClientInfoEntity;
import com.dianping.puma.core.entity.morphia.PumaClientInfoMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("pumaClientInfoDao")
public class PumaClientInfoMorphiaDao extends BasicDAO<PumaClientInfoMorphiaEntity, String> implements PumaClientInfoDao {

	@Autowired
	public PumaClientInfoMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
	
	@Override
	public void create(PumaClientInfoEntity entity) {
		PumaClientInfoMorphiaEntity morphiaEntity=new PumaClientInfoMorphiaEntity(entity);
		this.save(morphiaEntity);
		this.getDatastore().ensureIndexes();
	}

	@Override
	public PumaClientInfoEntity find(String id) {
		Query<PumaClientInfoMorphiaEntity> q = this.getDatastore().createQuery(PumaClientInfoMorphiaEntity.class);
		q.field("id").equal(id);
		PumaClientInfoMorphiaEntity morphiaEntity = this.findOne(q);
		return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
	}

	@Override
	public List<PumaClientInfoEntity> findAll() {
		Query<PumaClientInfoMorphiaEntity> q = this.getDatastore().createQuery(PumaClientInfoMorphiaEntity.class);
		QueryResults<PumaClientInfoMorphiaEntity> result = this.find(q);
		List<PumaClientInfoMorphiaEntity> morphiaEntities = result.asList();

		List<PumaClientInfoEntity> entities = new ArrayList<PumaClientInfoEntity>();
		for (PumaClientInfoMorphiaEntity morphiaEntity: morphiaEntities) {
			entities.add(morphiaEntity.getEntity());
		}
		return entities;
	}

	@Override
	public void remove(String id) {
		Query<PumaClientInfoMorphiaEntity> q = this.getDatastore().createQuery(PumaClientInfoMorphiaEntity.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}

	@Override
	public void update(PumaClientInfoEntity entity) {
		this.create(entity);
	}

}
