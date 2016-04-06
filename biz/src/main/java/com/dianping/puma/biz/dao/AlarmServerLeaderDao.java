package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.AlarmServerLeaderEntity;
import org.apache.ibatis.annotations.Param;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface AlarmServerLeaderDao {

    AlarmServerLeaderEntity find();

    void insert(AlarmServerLeaderEntity entity);

    int update(@Param("oriVersion") long oriVersion, @Param("entity") AlarmServerLeaderEntity entity);

    int delete(String host);
}
