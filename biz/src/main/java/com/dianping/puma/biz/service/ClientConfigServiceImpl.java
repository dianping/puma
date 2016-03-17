package com.dianping.puma.biz.service;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientConfigDao;
import com.dianping.puma.biz.entity.ClientConfigEntity;
import com.dianping.puma.common.service.ClientConfigService;
import com.dianping.puma.common.model.ClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientConfigServiceImpl implements ClientConfigService {

    @Autowired
    Converter converter;

    @Autowired
    ClientConfigDao clientConfigDao;

    @Override
    public void create(String clientName, ClientConfig clientConfig) {
        ClientConfigEntity entity = converter.convert(clientConfig, ClientConfigEntity.class);
        entity.setClientName(clientName);
        clientConfigDao.insert(entity);
    }

    @Override
    public int modify(String clientName, ClientConfig clientConfig) {
        ClientConfigEntity entity = converter.convert(clientConfig, ClientConfigEntity.class);
        entity.setClientName(clientName);
        return clientConfigDao.update(entity);
    }

    @Override
    public void replace(String clientName, ClientConfig clientConfig) {
        int result = modify(clientName, clientConfig);
        if (result == 0) {
            create(clientName, clientConfig);
        }
    }

    @Override
    public int remove(String clientName) {
        return clientConfigDao.delete(clientName);
    }
}
