package com.dianping.puma.storage.maintain.clean;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ExpiredDeleteStrategy implements DeleteStrategy {

	private int preservedDay;

	@Override
	public boolean canClean(File directory) {
		return false;
	}
}
