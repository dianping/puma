package com.dianping.puma.core.monitor;

import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.producer.exceptions.RemoteServiceInitFailedException;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.producer.Producer;
import com.dianping.swallow.producer.ProducerConfig;
import com.dianping.swallow.producer.ProducerMode;
import com.dianping.swallow.producer.impl.ProducerFactoryImpl;

public class SwallowEventPulisher implements EventPublisher {
    private String topic;
    private Producer producer;

    public void init() throws RemoteServiceInitFailedException {
        ProducerConfig config = new ProducerConfig();
        config.setMode(ProducerMode.SYNC_MODE);
        producer = ProducerFactoryImpl.getInstance().createProducer(Destination.topic(topic), config);
    }

    @Override
    public void publish(Event event) throws SendFailedException {
        producer.sendMessage(event, event.getSyncServerName());
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
