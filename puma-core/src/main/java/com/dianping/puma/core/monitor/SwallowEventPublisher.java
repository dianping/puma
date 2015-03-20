package com.dianping.puma.core.monitor;

import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.producer.exceptions.RemoteServiceInitFailedException;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.producer.Producer;
import com.dianping.swallow.producer.ProducerConfig;
import com.dianping.swallow.producer.ProducerMode;
import com.dianping.swallow.producer.impl.ProducerFactoryImpl;

public class SwallowEventPublisher implements EventPublisher {
    private String topic;
    private Producer producer;

    public void init() throws RemoteServiceInitFailedException {
        ProducerConfig config = new ProducerConfig();
        config.setMode(ProducerMode.SYNC_MODE);
        producer = ProducerFactoryImpl.getInstance().createProducer(Destination.topic(topic), config);
    }

    @Override
    public void publish(Event event) throws SendFailedException {
        if (event instanceof PumaTaskEvent) {
            if (event instanceof PumaTaskStateEvent) {
                producer.sendMessage(event);
            } else {
                String pumaServerId = ((PumaTaskEvent) event).getPumaServerId();
                producer.sendMessage(event, pumaServerId);
            }
        } else {
            if (event instanceof SyncTaskStateEvent) {
                producer.sendMessage(event);
            } else {
                producer.sendMessage(event, ((SyncTaskEvent) event).getSyncServerName());
            }
        }
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
