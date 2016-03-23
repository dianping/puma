package com.dianping.puma.biz.service.remote;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientConfigDao;
import com.dianping.puma.biz.entity.ClientConfigEntity;
import com.dianping.puma.common.model.ClientConfig;
import com.dianping.puma.common.service.PumaClientConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class RemoteClientConfigService implements PumaClientConfigService {

    @Autowired
    Converter converter;

    @Autowired
    ClientConfigDao clientConfigDao;

    @Override
    public int update(String clientName, ClientConfig clientConfig) {
        ClientConfigEntity entity = converter.convert(clientConfig, ClientConfigEntity.class);
        entity.setClientName(clientName);
        return clientConfigDao.update(entity);
    }
}
