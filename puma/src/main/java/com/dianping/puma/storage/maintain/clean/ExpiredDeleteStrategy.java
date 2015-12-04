package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.storage.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ExpiredDeleteStrategy implements DeleteStrategy {

	private final int preservedDay = 5;

	@Override
	public boolean canClean(File directory) {
		return DateUtils.expired(directory.getName(), DateUtils.getNowString(), preservedDay);
	}
}
