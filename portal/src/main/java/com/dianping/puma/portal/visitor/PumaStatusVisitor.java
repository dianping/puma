package com.dianping.puma.portal.visitor;

import com.dianping.puma.portal.model.PumaServerStatusDto;

import java.util.Collection;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaStatusVisitor {

    void visit(Collection<PumaServerStatusDto> root);

    void visit(PumaServerStatusDto item);

    void visit(PumaServerStatusDto item, PumaServerStatusDto.Client client);

    void visit(PumaServerStatusDto item, PumaServerStatusDto.Server server);

}
