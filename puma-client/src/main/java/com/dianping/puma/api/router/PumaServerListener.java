package com.dianping.puma.api.router;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface PumaServerListener {

	public void onChange(List<Pair<String, Float>> loadBalances);
}
