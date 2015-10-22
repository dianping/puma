package com.dianping.puma.storage.maintain.archive;

import java.io.File;

public interface ArchiveStrategy {

	boolean canArchive(File directory);
}
