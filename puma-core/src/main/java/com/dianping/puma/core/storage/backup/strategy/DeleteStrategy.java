package com.dianping.puma.core.storage.backup.strategy;

import java.io.File;

public interface DeleteStrategy {

	boolean canDelete(File file);
}
