package com.dianping.puma.storage.backup;

import java.io.File;

public class AlwaysDeleteStrategy implements DeleteStrategy {

	@Override
	public boolean canDelete(File file) {
		return true;
	}
}
