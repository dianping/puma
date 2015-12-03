package com.dianping.puma.storage.cache;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.Sequence;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class ChangedEventWithSequence {
    private final ChangedEvent changedEvent;
    private final Sequence sequence;

    public ChangedEventWithSequence(ChangedEvent changedEvent, Sequence sequence) {
        this.changedEvent = changedEvent;
        this.sequence = sequence;
    }

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }

    public Sequence getSequence() {
        return sequence;
    }
}
