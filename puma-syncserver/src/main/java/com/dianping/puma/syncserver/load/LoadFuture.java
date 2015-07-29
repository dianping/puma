package com.dianping.puma.syncserver.load;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class LoadFuture extends FutureTask<Integer> {

	public LoadFuture(Callable<Integer> caller) {
		super(caller);
	}
}
