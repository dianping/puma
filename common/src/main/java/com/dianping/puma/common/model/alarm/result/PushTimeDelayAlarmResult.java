package com.dianping.puma.common.model.alarm.result;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmResult extends AlarmResult {

    private String title = "Puma客户端报警";

    private String head = "Puma服务端向客户端推送binlog延时超过阈值:";

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public void setHead(String head) {
        this.head = head;
    }
}
