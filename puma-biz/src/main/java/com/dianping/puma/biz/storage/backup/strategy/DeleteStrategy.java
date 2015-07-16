package com.dianping.puma.biz.storage.backup.strategy;

import java.io.File;

public interface DeleteStrategy {

	boolean canDelete(File file);
}
