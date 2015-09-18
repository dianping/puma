package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.lock.PumaClientLock;
import com.dianping.puma.api.lock.PumaClientLockListener;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.*;
import com.dianping.puma.core.util.sql.DMLType;
import com.google.common.collect.Lists;
import org.apache.log4j.BasicConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dianping.puma.core.event.RowChangedEvent.*;

public class ClusterPumaClientMainTest {

	public static void main(String[] args) {

		BasicConfigurator.configure();

		List<String> tables = new ArrayList<String>();
		tables.add("UOD_Order0");
		tables.add("UOD_Order1");
		tables.add("UOD_OrderExtraFields0");
		tables.add("UOD_OrderExtraFields1");
		tables.add("UOD_OrderLog0");
		tables.add("UOD_OrderLog1");
		tables.add("UOD_OrderPaymentDetail0");
		tables.add("UOD_OrderPaymentDetail1");
		tables.add("UOD_OrderSKU0");
		tables.add("UOD_OrderSKU1");
		tables.add("UOD_OrderSKUExtraFields0");
		tables.add("UOD_OrderSKUExtraFields1");

		PumaClient client = new PumaClientConfig()
				.setClientName("dozer-debug")
				.setDatabase("UnifiedOrder0")
				.setTables(tables)
				.setDdl(true)
				.setDml(true)
				.setTransaction(true)
						//                .buildClusterPumaClient();
				.setServerHosts(Lists.newArrayList("127.0.0.1:4040"))
				.buildFixedClusterPumaClient();

		final int size = 100;

		PumaClientLock lock = new PumaClientLock("dozer-debug");
		final boolean[] a = { true };
		try {
			lock.lock(new PumaClientLockListener() {
				@Override public void onLost() {
					a[0] = false;
				}
			});

			while (a[0]) {
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				try {
					BinlogMessage message = client.get(size, 1, TimeUnit.SECONDS);
					client.ack(message.getLastBinlogInfo());
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}
		} catch (Throwable t) {

		} finally {
			lock.unlockQuietly();
		}

		System.out.println("##################################################");
	}
}
