package com.dianping.puma.storage.backup;

import java.io.File;

public interface DeleteStrategy {

	boolean canDelete(File file);
}
