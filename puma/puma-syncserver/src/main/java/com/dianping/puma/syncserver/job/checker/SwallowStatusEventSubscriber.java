package com.dianping.puma.syncserver.job.checker;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.notify.EventListener;
import com.dianping.puma.core.sync.model.notify.EventSubscriber;
import com.dianping.puma.core.sync.model.notify.SyncTaskStatusActionEvent;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.swallow.common.consumer.ConsumerType;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.Consumer;
import com.dianping.swallow.consumer.ConsumerConfig;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.swallow.consumer.impl.ConsumerFactoryImpl;

public class SwallowStatusEventSubscriber implements EventSubscriber {
    private final String topic = "puma_status_action_event";
    private EventListener listener;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private Config config;

    public SwallowStatusEventSubscriber() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setConsumerType(ConsumerType.NON_DURABLE);
        Consumer c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(topic), config.getSyncServerName(),
                consumerConfig);
        c.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                try {
                    SyncTaskStatusActionEvent event = msg.transferContentToBean(SyncTaskStatusActionEvent.class);
                    listener.onEvent(event);
                } catch (RuntimeException e) {
                    notifyService.alarm(e.getMessage(), e, false);
                }
            }
        });
        c.start();
    }

    @Override
    public void setEventListener(EventListener listener) {
        this.listener = listener;
    }

}
