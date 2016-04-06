package com.dianping.puma.alarm.ha;

import com.dianping.puma.alarm.exception.PumaAlarmServerLeaderManageException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerLeaderManager extends PumaLifeCycle {

    String findLeader() throws PumaAlarmServerLeaderManageException;

    boolean tryTakeLeader() throws PumaAlarmServerLeaderManageException;

    void releaseLeader() throws PumaAlarmServerLeaderManageException;

    void addLeaderChangeListener(LeaderChangeListener listener);
}
