package com.dianping.puma.alarm.ha;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface LeaderChangeListener {

    void onLeaderTaken();

    void onLeaderReleased();
}
