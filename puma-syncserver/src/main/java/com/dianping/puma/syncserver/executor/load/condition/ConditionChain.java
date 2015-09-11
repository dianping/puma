package com.dianping.puma.syncserver.executor.load.condition;

public interface ConditionChain extends Condition {

	public void addCondition(Condition condition);
}
