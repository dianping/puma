package com.dianping.puma.syncserver.job.checker;

import org.springframework.stereotype.Service;

import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.sync.model.notify.Event;
import com.dianping.puma.core.sync.model.notify.EventPublisher;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.producer.exceptions.RemoteServiceInitFailedException;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.producer.Producer;
import com.dianping.swallow.producer.ProducerConfig;
import com.dianping.swallow.producer.ProducerMode;
import com.dianping.swallow.producer.impl.ProducerFactoryImpl;

@Service
public class SwallowStatusEventPulisher implements EventPublisher {

    private final Producer producer;
    private final String topic = "puma_status_event";
    private NotifyService notifyService;

    public SwallowStatusEventPulisher() throws RemoteServiceInitFailedException {
        ProducerConfig config = new ProducerConfig();
        config.setMode(ProducerMode.SYNC_MODE);
        producer = ProducerFactoryImpl.getInstance().createProducer(Destination.topic(topic), config);
    }

    @Override
    public void publish(Event event) {
        try {
            producer.sendMessage(event, event.getSyncServerName());
        } catch (SendFailedException e) {
            notifyService.alarm(e.getMessage(), e, false);
        }
    }

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

}
