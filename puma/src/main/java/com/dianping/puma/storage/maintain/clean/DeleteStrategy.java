package com.dianping.puma.storage.maintain.clean;

import java.io.File;

public interface DeleteStrategy {

	boolean canClean(File directory);
}
