package com.dianping.puma.syncserver.job.checker;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.notify.EventListener;
import com.dianping.puma.core.sync.model.notify.EventSubscriber;
import com.dianping.puma.core.sync.model.notify.TaskStatusEvent;
import com.dianping.swallow.common.consumer.ConsumerType;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.Consumer;
import com.dianping.swallow.consumer.ConsumerConfig;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.swallow.consumer.impl.ConsumerFactoryImpl;

public class SwallowTaskEventSubscriber implements EventSubscriber {
    private final String topic = "puma_status_event";
    private EventListener listener;
    private NotifyService notifyService;

    public SwallowTaskEventSubscriber() {
        ConsumerConfig config = new ConsumerConfig();
        config.setConsumerType(ConsumerType.NON_DURABLE);
        Consumer c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(topic), config);
        c.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                try {
                    TaskStatusEvent event = msg.transferContentToBean(TaskStatusEvent.class);
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
