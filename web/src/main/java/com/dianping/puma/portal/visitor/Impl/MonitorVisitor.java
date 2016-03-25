package com.dianping.puma.portal.visitor.Impl;

import com.dianping.cat.Cat;
import com.dianping.puma.portal.model.PumaServerStatusDto;
import com.dianping.puma.portal.visitor.PumaStatusVisitor;

import java.util.Collection;

/**
 * Dozer @ 2016-01
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class MonitorVisitor implements PumaStatusVisitor {

    @Override
    public void visit(Collection<PumaServerStatusDto> root) {
        for (PumaServerStatusDto item : root) {
            visit(item);
        }
    }

    @Override
    public void visit(PumaServerStatusDto item) {
        for (PumaServerStatusDto.Server server : item.getServers().values()) {
            server.setServer(item.getName());
            visit(item, server);
        }
        for (PumaServerStatusDto.Client client : item.getClients().values()) {
            visit(item, client);
        }
    }

    @Override
    public void visit(PumaServerStatusDto item, PumaServerStatusDto.Client client) {
        if (client.getAckBinlogInfo() != null && client.getAckBinlogInfo().getTimestamp() < (System.currentTimeMillis() / 1000 - 2 * 24 * 60 * 60)) {
            Exception exp = new IllegalStateException(
                    String.format("%s ack delay:%d",
                            client.getName(),
                            (System.currentTimeMillis() / 1000 - client.getAckBinlogInfo().getTimestamp())
                    ));
            logError(exp);
        }
    }

    @Override
    public void visit(PumaServerStatusDto item, PumaServerStatusDto.Server server) {
        if (server.getBinlogInfo() != null && server.getBinlogInfo().getTimestamp() < (System.currentTimeMillis() / 1000 - 60 * 60)) {
            Exception exp = new IllegalStateException(
                    String.format("%s binlog delay:%d",
                            server.getName(),
                            (System.currentTimeMillis() / 1000 - server.getBinlogInfo().getTimestamp())
                    ));
            logError(exp);
        }
    }

    protected void logError(Exception exp) {
        Cat.logError(exp.getMessage(), exp);
    }
}
