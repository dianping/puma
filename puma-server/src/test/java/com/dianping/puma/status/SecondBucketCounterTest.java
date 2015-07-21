package com.dianping.puma.status;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

public class SecondBucketCounterTest {
	
	@Test
	public void test() throws InterruptedException{
		SecondBucketCounter counter = new SecondBucketCounter();
		
		counter.increase();
		counter.increase();
		counter.increase();
		counter.increase();
		counter.increase();

		Assert.assertEquals(5, counter.get());
		
		TimeUnit.SECONDS.sleep(2);
		
		counter.increase();
		counter.increase();
		counter.increase();
		
		Assert.assertEquals(3, counter.get());
	}

}
