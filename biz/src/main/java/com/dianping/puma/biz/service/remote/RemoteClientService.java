package com.dianping.puma.biz.service.remote;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.*;
import com.dianping.puma.common.model.Client;
import com.dianping.puma.common.service.PumaClientService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientService implements PumaClientService {

    private Converter converter;

    private ClientDao clientDao;

    private ClientAdditionDao clientAdditionDao;

    private ClientConfigDao clientConfigDao;

    private ClientConnectDao clientConnectDao;

    private ClientAckDao clientAckDao;

    private ClientAlarmDataDao clientAlarmDataDao;

    private ClientAlarmBenchmarkDao clientAlarmBenchmarkDao;

    private ClientAlarmStrategyDao clientAlarmStrategyDao;

    private ClientAlarmMetaDao clientAlarmMetaDao;

    private ClientTokenDao clientTokenDao;

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public Client findByClientName(String clientName) {
        ClientEntity clientEntity = clientDao.find(clientName);
        return converter.convert(clientEntity, Client.class);
    }

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
    public List<String> findAllClientNames() {
        return FluentIterable
                .from(findAll())
                .transform(new Function<Client, String>() {
                    @Override
                    public String apply(Client client) {
                        return client.getClientName();
                    }
                }).toList();
    }

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public void create(Client client) {
        ClientEntity clientEntity
                = converter.convert(client, ClientEntity.class);
        clientDao.insert(clientEntity);

        ClientAdditionEntity clientAdditionEntity
                = converter.convert(client, ClientAdditionEntity.class);
        clientAdditionDao.insert(clientAdditionEntity);

        ClientConfigEntity clientConfigEntity
                = converter.convert(client, ClientConfigEntity.class);
        clientConfigDao.insert(clientConfigEntity);

        ClientConnectEntity clientConnectEntity
                = converter.convert(client, ClientConnectEntity.class);
        clientConnectDao.insert(clientConnectEntity);

        ClientAckEntity clientAckEntity
                = converter.convert(client, ClientAckEntity.class);
        clientAckDao.insert(clientAckEntity);

        ClientAlarmDataEntity clientAlarmDataEntity
                = converter.convert(client, ClientAlarmDataEntity.class);
        clientAlarmDataDao.insert(clientAlarmDataEntity);

        ClientAlarmBenchmarkEntity clientAlarmBenchmarkEntity
                = converter.convert(client, ClientAlarmBenchmarkEntity.class);
        clientAlarmBenchmarkDao.insert(clientAlarmBenchmarkEntity);

        ClientAlarmStrategyEntity clientAlarmStrategyEntity
                = converter.convert(client, ClientAlarmStrategyEntity.class);
        clientAlarmStrategyDao.insert(clientAlarmStrategyEntity);

        ClientAlarmMetaEntity clientAlarmMetaEntity
                = converter.convert(client, ClientAlarmMetaEntity.class);
        clientAlarmMetaDao.insert(clientAlarmMetaEntity);

        ClientTokenEntity clientTokenEntity
                = converter.convert(client, ClientTokenEntity.class);
        clientTokenDao.insert(clientTokenEntity);
    }

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public void update(Client client) {
        ClientEntity clientEntity
                = converter.convert(client, ClientEntity.class);
        clientDao.update(clientEntity);

        ClientAdditionEntity clientAdditionEntity
                = converter.convert(client, ClientAdditionEntity.class);
        clientAdditionDao.update(clientAdditionEntity);

        ClientConfigEntity clientConfigEntity
                = converter.convert(client, ClientConfigEntity.class);
        clientConfigDao.update(clientConfigEntity);

        ClientConnectEntity clientConnectEntity
                = converter.convert(client, ClientConnectEntity.class);
        clientConnectDao.update(clientConnectEntity);

        ClientAckEntity clientAckEntity
                = converter.convert(client, ClientAckEntity.class);
        clientAckDao.update(clientAckEntity);

        ClientAlarmDataEntity clientAlarmDataEntity
                = converter.convert(client, ClientAlarmDataEntity.class);
        clientAlarmDataDao.update(clientAlarmDataEntity);

        ClientAlarmBenchmarkEntity clientAlarmBenchmarkEntity
                = converter.convert(client, ClientAlarmBenchmarkEntity.class);
        clientAlarmBenchmarkDao.update(clientAlarmBenchmarkEntity);

        ClientAlarmStrategyEntity clientAlarmStrategyEntity
                = converter.convert(client, ClientAlarmStrategyEntity.class);
        clientAlarmStrategyDao.update(clientAlarmStrategyEntity);

        ClientAlarmMetaEntity clientAlarmMetaEntity
                = converter.convert(client, ClientAlarmMetaEntity.class);
        clientAlarmMetaDao.update(clientAlarmMetaEntity);
    }

    @Override
    @Transactional(value = "pumaTransactionManager", rollbackFor = Throwable.class)
    public int remove(String clientName) {
        int result = clientDao.delete(clientName);

        clientAdditionDao.delete(clientName);
        clientConfigDao.delete(clientName);
        clientConnectDao.delete(clientName);
        clientAckDao.delete(clientName);
        clientAlarmDataDao.delete(clientName);
        clientAlarmBenchmarkDao.delete(clientName);
        clientAlarmStrategyDao.delete(clientName);
        clientAlarmMetaDao.delete(clientName);

        return result;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientDao(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setClientAdditionDao(ClientAdditionDao clientAdditionDao) {
        this.clientAdditionDao = clientAdditionDao;
    }

    public void setClientConfigDao(ClientConfigDao clientConfigDao) {
        this.clientConfigDao = clientConfigDao;
    }

    public void setClientConnectDao(ClientConnectDao clientConnectDao) {
        this.clientConnectDao = clientConnectDao;
    }

    public void setClientAckDao(ClientAckDao clientAckDao) {
        this.clientAckDao = clientAckDao;
    }

    public void setClientAlarmDataDao(ClientAlarmDataDao clientAlarmDataDao) {
        this.clientAlarmDataDao = clientAlarmDataDao;
    }

    public void setClientAlarmBenchmarkDao(ClientAlarmBenchmarkDao clientAlarmBenchmarkDao) {
        this.clientAlarmBenchmarkDao = clientAlarmBenchmarkDao;
    }

    public void setClientAlarmStrategyDao(ClientAlarmStrategyDao clientAlarmStrategyDao) {
        this.clientAlarmStrategyDao = clientAlarmStrategyDao;
    }

    public void setClientAlarmMetaDao(ClientAlarmMetaDao clientAlarmMetaDao) {
        this.clientAlarmMetaDao = clientAlarmMetaDao;
    }

    public void setClientTokenDao(ClientTokenDao clientTokenDao) {
        this.clientTokenDao = clientTokenDao;
    }
}
