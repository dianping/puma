package com.dianping.puma.core.storage.backup.strategy;

import org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.util.Date;

public class ExpiredDeleteStrategy implements DeleteStrategy {

	private int expireDate;

	public ExpiredDeleteStrategy() {}

	public ExpiredDeleteStrategy(int expireDate) {
		this.expireDate = expireDate;
	}

	@Override
	public boolean canDelete(File fileToClean) {
		return DateUtils.addDays(new Date(fileToClean.lastModified()), expireDate).after(new Date());
	}

	public int getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(int expireDate) {
		this.expireDate = expireDate;
	}
}
