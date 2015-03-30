package com.dianping.puma.core.storage.backup.strategy;

import java.io.File;

public class AlwaysDeleteStrategy implements DeleteStrategy {

	@Override
	public boolean canDelete(File file) {
		return true;
	}
}
