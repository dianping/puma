package com.dianping.puma.core.sync.dao;

/**
 * @author Leo Liang
 */
public interface SeqGeneratorService {

    public long nextSeq(String category);

}
