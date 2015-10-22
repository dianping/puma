package com.dianping.puma.storage.filesystem;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service
public class DefaultFileSystem implements FileSystem {

	@Override public File getL1IndexDir() {
		return null;
	}

	@Override public File getL2IndexDir() {
		return null;
	}

	@Override public File getMasterDataDir() {
		return null;
	}

	@Override public File getSlaveDataDir() {
		return null;
	}

	@Override public File[] visitL1IndexDateDirs() {
		return new File[0];
	}

	@Override public File[] visitL1IndexDateDirs(String database) {
		return new File[0];
	}

	@Override public File[] visitL2IndexDateDirs() {
		return new File[0];
	}

	@Override public File[] visitL2IndexDateDirs(String database) {
		return new File[0];
	}

	@Override public File[] visitMasterDataDateDirs() {
		return new File[0];
	}

	@Override public File[] visitMasterDataDateDirs(String database) {
		return new File[0];
	}

	@Override public File[] visitSlaveDataDateDirs() {
		return new File[0];
	}

	@Override public File[] visitSlaveDataDateDirs(String database) {
		return new File[0];
	}

	@Override public Date parseDateDir(File dateDir) {
		return null;
	}

	@Override public File createDateDir(File baseDir, Date date) {
		return null;
	}
}
