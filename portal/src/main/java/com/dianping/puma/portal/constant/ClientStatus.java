package com.dianping.puma.portal.constant;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public enum ClientStatus {

    /** 正常运行 */
    RUNNING,

    /** 警告 */
    EXCEPTION;

    public boolean isRunning() {
        return this.equals(ClientStatus.RUNNING);
    }

    public boolean isException() {
        return this.equals(ClientStatus.EXCEPTION);
    }
}
