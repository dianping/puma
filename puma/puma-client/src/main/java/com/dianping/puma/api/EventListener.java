package com.dianping.puma.api;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventListener {
    /**
     * 事件到达回调函数
     * 
     * @param event 事件
     * @throws Exception
     */
    public void onEvent(ChangedEvent event) throws Exception;

    /**
     * 事件异常回调函数
     * 
     * @param event 事件
     * @param e 发生的异常
     * @return 是否可以跳过此事件
     */
    public boolean onException(ChangedEvent event, Exception e);

    /**
     * 连接事件异常回调函数
     * 
     * @param e 发生的异常
     */
    public void onConnectException(Exception e);

    /**
     * 连接事件回调函数
     */
    public void onConnected();

    /**
     * 事件跳过时候的回调函数
     * 
     * @param event 被跳过的事件
     */
    public void onSkipEvent(ChangedEvent event);
}
