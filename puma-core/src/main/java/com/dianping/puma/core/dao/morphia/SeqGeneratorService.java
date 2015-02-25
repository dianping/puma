package com.dianping.puma.core.dao.morphia;

/**
 * @author Leo Liang
 */
public interface SeqGeneratorService {

    public long nextSeq(String category);

}
