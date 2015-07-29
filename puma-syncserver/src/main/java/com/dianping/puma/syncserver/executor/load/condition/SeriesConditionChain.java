package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.event.ChangedEvent;

import java.util.ArrayList;
import java.util.List;

public class SeriesConditionChain implements ConditionChain {

	protected List<Condition> conditions = new ArrayList<Condition>();

	@Override
	public void addCondition(Condition condition) {
		conditions.add(condition);
	}

	@Override
	public void reset() {
		for (Condition condition: conditions) {
			condition.reset();
		}
	}

	@Override
	public boolean isLocked(ChangedEvent binlogEvent) {
		for (Condition condition: conditions) {
			if (condition.isLocked(binlogEvent)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void lock(ChangedEvent binlogEvent) {
		for (Condition condition: conditions) {
			condition.lock(binlogEvent);
		}
	}

	@Override
	public void unlock(ChangedEvent binlogEvent) {
		for (Condition condition: conditions) {
			condition.unlock(binlogEvent);
		}
	}
}
