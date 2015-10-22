package com.dianping.puma.storage.maintain.archive;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ExpiredArchiveStrategy implements ArchiveStrategy {

	@Override
	public boolean canArchive(File directory) {
		return false;
	}
}
