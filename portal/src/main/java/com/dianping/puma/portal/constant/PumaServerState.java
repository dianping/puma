package com.dianping.puma.portal.constant;

/**
 * Created by xiaotian.li on 16/4/6.
 * Email: lixiaotian07@gmail.com
 */
public enum PumaServerState {

    /** 在线 */
    ONLINE,
    /** 不在线 */
    OFFLINE,
    /** 备用 */
    STANDBY;

    public boolean isOnline() {
        return this.equals(PumaServerState.ONLINE);
    }

    public boolean isOffline() {
        return this.equals(PumaServerState.OFFLINE);
    }

    public boolean isStandby() {
        return this.equals(PumaServerState.STANDBY);
    }
}
