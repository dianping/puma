package com.dianping.puma.core.monitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.puma.core.monitor.event.Event;
import com.dianping.swallow.common.consumer.ConsumerType;
import com.dianping.swallow.common.consumer.MessageFilter;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.Consumer;
import com.dianping.swallow.consumer.ConsumerConfig;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.swallow.consumer.impl.ConsumerFactoryImpl;

public class SwallowEventSubscriber implements EventSubscriber {
    private String topic, type;
    private List<EventListener> listeners;
    private NotifyService notifyService;
    private Class<Event> clazz;

    public void init() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setConsumerType(ConsumerType.NON_DURABLE);
        Consumer c;
        if (type != null) {
            Set<String> matchedTypes = new HashSet<String>();
            matchedTypes.add(type);
            consumerConfig.setMessageFilter(MessageFilter.createInSetMessageFilter(matchedTypes));
        }
        c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic(topic), consumerConfig);
        c.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                try {
                    Event event = msg.transferContentToBean(clazz);
                    if (listeners != null) {
                        for (EventListener listener : listeners) {
                            listener.onEvent(event);
                        }
                    }
                } catch (RuntimeException e) {
                    notifyService.alarm(e.getMessage(), e, false);
                }
            }
        });
        c.start();
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setListeners(List<EventListener> listeners) {
        this.listeners = listeners;
    }

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public void setClazz(Class<Event> clazz) {
        this.clazz = clazz;
    }

}
