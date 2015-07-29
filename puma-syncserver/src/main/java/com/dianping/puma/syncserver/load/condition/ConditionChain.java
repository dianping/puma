package com.dianping.puma.syncserver.load.condition;

public interface ConditionChain extends Condition {

	public void addCondition(Condition condition);
}
