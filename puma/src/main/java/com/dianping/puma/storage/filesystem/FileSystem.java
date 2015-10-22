package com.dianping.puma.storage.filesystem;

import java.io.File;
import java.util.Date;

public interface FileSystem {

	public File getL1IndexDir();

	public File getL2IndexDir();

	public File getMasterDataDir();

	public File getSlaveDataDir();

	public File[] visitL1IndexDateDirs();

	public File[] visitL1IndexDateDirs(String database);

	public File[] visitL2IndexDateDirs();

	public File[] visitL2IndexDateDirs(String database);

	public File[] visitMasterDataDateDirs();

	public File[] visitMasterDataDateDirs(String database);

	public File[] visitSlaveDataDateDirs();

	public File[] visitSlaveDataDateDirs(String database);

	public Date parseDateDir(File dateDir);

	public File createDateDir(File baseDir, Date date);
}
