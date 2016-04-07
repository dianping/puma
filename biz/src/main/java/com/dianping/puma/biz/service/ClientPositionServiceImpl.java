package com.dianping.puma.biz.service;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.ClientPositionDao;
import com.dianping.puma.biz.entity.ClientPositionEntity;
import com.dianping.puma.common.model.ClientPosition;
import com.dianping.puma.common.service.ClientPositionService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ClientPositionServiceImpl implements ClientPositionService {

    private final Logger logger = LoggerFactory.getLogger(ClientPositionServiceImpl.class);

    private Converter converter;

    private ClientPositionDao clientPositionDao;

    private Map<String, ClientPosition> positionMap = new ConcurrentHashMap<String, ClientPosition>();

    @Override
    public ClientPosition find(String clientName) {
        ClientPositionEntity entity = clientPositionDao.findByClientName(clientName);
        return converter.convert(entity, ClientPosition.class);
    }

    @Override
    public List<ClientPosition> findAll() {
        List<ClientPositionEntity> entities = clientPositionDao.findAll();
        return FluentIterable.from(entities)
                .transform(new Function<ClientPositionEntity, ClientPosition>() {
                    @Override
                    public ClientPosition apply(ClientPositionEntity entity) {
                        return converter.convert(entity, ClientPosition.class);
                    }
                }).toList();
    }

    @Override
    public void update(ClientPosition clientPosition) {
        positionMap.put(clientPosition.getClientName(), clientPosition);
    }

    @Scheduled(fixedDelay = 5000)
    public void flush() {
        Set<String> keys = positionMap.keySet();
        for (String key : keys) {
            ClientPosition clientPosition = positionMap.remove(key);
            if (clientPosition == null) {
                continue;
            }

            try {
                ClientPositionEntity entity = converter.convert(clientPosition, ClientPositionEntity.class);
                int updateRow = clientPositionDao.update(entity);
                if (updateRow == 0) {
                    clientPositionDao.insert(entity);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void cleanUpTestClients() {
        List<ClientPositionEntity> clients = clientPositionDao.findOldTestClient();
        for (ClientPositionEntity entity : clients) {
            clientPositionDao.delete(entity.getId());
        }
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientPositionDao(ClientPositionDao clientPositionDao) {
        this.clientPositionDao = clientPositionDao;
    }
}
