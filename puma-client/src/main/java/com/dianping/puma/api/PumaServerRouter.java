package com.dianping.puma.api;

import com.dianping.puma.api.exception.PumaClientException;

public interface PumaServerRouter {

	public String next() throws PumaClientException;
}
