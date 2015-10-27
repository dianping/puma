package com.dianping.puma.storage.maintain.archive;

import com.dianping.puma.storage.filesystem.FileSystem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.GZIPOutputStream;

@Service
public final class ScheduledArchiveService implements ArchiveService {

	private static final int GZIP_BUF_SIZE = 1024;

	@Autowired
	ArchiveStrategy archiveStrategy;

	@Override
	public void archive() {
		File[] masterDataDateDirs = FileSystem.visitMasterDataDateDirs();
		File slaveDataDir = FileSystem.getSlaveDataDir();
		for (File masterDataDateDir: masterDataDateDirs) {
		}
	}

	private void archive(File srcDirectory, File dstDirectory) {
		if (archiveStrategy.canArchive(srcDirectory)) {
			try {
				archiveDirectory(srcDirectory, dstDirectory);
			} catch (IOException ignore) {
			}
		}
	}

	protected void archiveDirectory(File srcDirectory, File dstDirectory) throws IOException {
		File[] srcFiles = srcDirectory.listFiles();
		if (srcFiles != null) {
			for (File srcFile: srcFiles) {
				File dstFile = createDstFile(dstDirectory, srcFile);
				archiveFile(srcFile, dstFile);
			}
		}

		deleteDirectory(srcDirectory);
	}

	protected File createDstFile(File dstDirectory, File srcFile) throws IOException {
		File dstFile = new File(dstDirectory, srcFile.getName());
		File parent = srcFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IOException("failed to create destination file parents.");
		}

		if (!dstFile.createNewFile()) {
			throw new IOException("failed to create destination file.");
		}

		return dstFile;
	}

	protected void archiveFile(File srcFile, File dstFile) throws IOException {
		FileInputStream is = new FileInputStream(srcFile);
		GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(dstFile));

		byte[] data = new byte[GZIP_BUF_SIZE];
		int count;

		while ((count = is.read(data)) > 0) {
			gos.write(data, 0, count);
		}

		gos.close();
		is.close();
	}

	protected void deleteDirectory(File directory) throws IOException {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (FileNotFoundException ignore) {
		}
	}

	@Scheduled(cron = "0 0 3 * * *")
	public void scheduledArchive() {
		archive();
	}
}
