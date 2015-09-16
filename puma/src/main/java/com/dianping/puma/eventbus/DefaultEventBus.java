package com.dianping.puma.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * Dozer @ 15/8/27
 * mail@dozer.cc
 * http://www.dozer.cc
 */

public class DefaultEventBus extends EventBus {
    public static final DefaultEventBus INSTANCE = new DefaultEventBus();
}