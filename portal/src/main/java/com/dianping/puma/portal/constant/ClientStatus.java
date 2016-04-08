package com.dianping.puma.portal.constant;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public enum ClientStatus {

    /** 运行结束 */
    SUCCESS,

    /** 正常运行 */
    RUNNING,

    /** 警告 */
    WARNING,

    /** 错误 */
    ERROR;

    public boolean isSuccess() {
        return this.equals(ClientStatus.SUCCESS);
    }

    public boolean isRunning() {
        return this.equals(ClientStatus.RUNNING);
    }

    public boolean isWarning() {
        return this.equals(ClientStatus.WARNING);
    }

    public boolean isError() {
        return this.equals(ClientStatus.ERROR);
    }
}
