package com.dianping.puma.syncserver;

import com.dianping.puma.api.EventListener;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.core.event.ChangedEvent;

import java.util.List;

public class PumaClientMainTest {

	public PumaClient createPumaClient() {
		PumaClient pumaClient = new PumaClient();
		pumaClient.setName("lixt");
		pumaClient.setDatabase("DPShop");
		List<String> tables = new

		pumaClient.register(new EventListener() {
			@Override public void onEvent(ChangedEvent event) {
				System.out.println(event.toString());
			}
		});

		return pumaClient;
	}

	public static void main(String []args){
		PumaClientMainTest main = new PumaClientMainTest();
		final PumaClient pumaClient = main.createPumaClient();
		pumaClient.start();
	}
}
