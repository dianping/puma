package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.*;
import com.dianping.puma.common.model.Client;
import com.dianping.puma.biz.service.ClientService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    Converter converter;

    @Autowired
    ClientDao clientDao;

    @Autowired
    ClientAdditionDao clientAdditionDao;

    @Autowired
    ClientConfigDao clientConfigDao;

    @Autowired
    ClientConnectDao clientConnectDao;

    @Autowired
    ClientAckDao clientAckDao;

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public List<Client> findAll() {
        List<ClientEntity> clientEntities = clientDao.findAll();

        return FluentIterable
                .from(clientEntities)
                .transform(new Function<ClientEntity, Client>() {
                    @Override
                    public Client apply(ClientEntity clientEntity) {
                        return converter.convert(clientEntity, Client.class);
                    }
                }).toList();
    }

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public void create(Client client) {
        ClientEntity clientEntity = converter.convert(client, ClientEntity.class);
        clientDao.insert(clientEntity);

        ClientAdditionEntity clientAdditionEntity = converter.convert(client, ClientAdditionEntity.class);
        clientAdditionDao.insert(clientAdditionEntity);

        ClientConfigEntity clientConfigEntity = converter.convert(client, ClientConfigEntity.class);
        clientConfigDao.insert(clientConfigEntity);

        ClientConnectEntity clientConnectEntity = converter.convert(client, ClientConnectEntity.class);
        clientConnectDao.insert(clientConnectEntity);

        ClientAckEntity clientAckEntity = converter.convert(client, ClientAckEntity.class);
        clientAckDao.insert(clientAckEntity);
    }
}
