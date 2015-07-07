package com.dianping.puma.biz.monitor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TransactionMonitor extends AbstractPumaMonitor {

	private ConcurrentMap<String, Transaction> transactions = new ConcurrentHashMap<String, Transaction>();

	public TransactionMonitor(String type) {
		super(type);
	}

	public TransactionMonitor(String type, Long countThreshold) {
		super(type, countThreshold);
	}

	public void recordBegin(String name) {
		if (!isStopped()) {
			startCountingIfNeeded(name);
			incrCountingIfExists(name);
			if (checkCountingIfExists(name)) {
				transactions.put(name, Cat.newTransaction(this.type, name));
			}
		}
	}

	public void recordEnd(String name) {
		if (!isStopped()) {
			if (checkCountingIfExists(name)) {
				resetCountingIfExists(name);
				Transaction transaction = transactions.get(name);
				if (transaction != null) {
					transaction.complete();
					transactions.remove(name);
				}
			}
		}
	}

	@Override
	public void record(Object name, Object status) {
		if (isStopped()) {
			if (checkCountingIfExists((String) name)) {
				Transaction transaction = transactions.get(name);
				if (transaction != null) {
					transaction.setStatus((String) status);
				}
			}
		}
	}

	@Override
	protected void doStart() {}

	@Override
	protected void doStop() {}

	@Override
	protected void doPause() {}

	@Override
	public void remove(Object name) {
		// TODO Auto-generated method stub
		
	}
}
