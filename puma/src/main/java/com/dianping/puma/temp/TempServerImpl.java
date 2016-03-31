package com.dianping.puma.temp;

import com.dianping.puma.common.model.Client;
import com.dianping.puma.common.model.ClientPosition;
import com.dianping.puma.common.service.ClientPositionService;
import com.dianping.puma.common.service.PumaClientService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class TempServerImpl implements TempServer, InitializingBean {

    @Autowired
    PumaClientService pumaClientService;

    @Autowired
    ClientPositionService clientPositionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ClientPosition> clientPositions = clientPositionService.findAll();
        for (ClientPosition clientPosition: clientPositions) {
            String clientName = clientPosition.getClientName();

            Client client = new Client();
            client.setClientName(clientName);

            pumaClientService.create(client);
        }
    }
}
