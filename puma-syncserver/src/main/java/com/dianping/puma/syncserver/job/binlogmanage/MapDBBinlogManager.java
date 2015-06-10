package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.annotation.ThreadSafe;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MapDBBinlogManager implements BinlogManager {

	private static final Logger LOG = LoggerFactory.getLogger(MapDBBinlogManager.class);

	private boolean inited = false;

	private boolean stopped = true;

	private String name;

	private String folderName;

	private MappedByteBuffer mbb;

	private ConcurrentNavigableMap<Long, BinlogInfo> unfinished;

	private ConcurrentNavigableMap<Long, BinlogInfo> finished;

	private long oriSeq;

	private BinlogInfo oriBinlogInfo;

	public MapDBBinlogManager(long oriSeq, BinlogInfo oriBinlogInfo) {
		this.oriSeq = oriSeq;
		this.oriBinlogInfo = oriBinlogInfo;
	}

	@Override
	public void init() {
		if (inited) {
			return;
		}

		createFolderIfNeeded(folderName);
		createFileIfNeededAndMapToBuffer(folderName, name);

		finished = new ConcurrentSkipListMap<Long, BinlogInfo>();
		unfinished = new ConcurrentSkipListMap<Long, BinlogInfo>();

		inited = true;
	}

	@Override
	public void destroy() {
		if (!inited) {
			return;
		}

		mbb = null;
		finished = null;
		unfinished = null;

		deleteFile(folderName, name);
	}

	@Override
	public void start() {
		if (!stopped) {
			return;
		}

		stopped = false;

		loadBreakpoint();
	}

	@Override
	public void stop() {
		if (stopped) {
			return;
		}

		stopped = true;

		finished.clear();
		unfinished.clear();
	}

	@ThreadSafe
	@Override
	public void before(long seq, BinlogInfo binlogInfo) {
		unfinished.put(seq, binlogInfo);
	}

	@ThreadSafe
	@Override
	public void after(long seq, BinlogInfo binlogInfo) {
		finished.put(seq, binlogInfo);

		if (finished.size() > 100) {
			finished.pollFirstEntry();
		}

		unfinished.remove(seq);
	}

	@Override
	public BinlogInfo getBinlogInfo() {
		if (unfinished.lastEntry() != null) {
			return unfinished.lastEntry().getValue();
		} else {
			if (finished.firstEntry() != null) {
				return finished.firstEntry().getValue();
			} else {
				return oriBinlogInfo;
			}
		}
	}

	@Override
	public long getSeq() {
		if (unfinished.lastEntry() != null) {
			return unfinished.lastEntry().getKey();
		} else {
			if (finished.firstEntry() != null) {
				return finished.firstEntry().getKey();
			} else {
				return oriSeq;
			}
		}
	}

	private void createFolderIfNeeded(String folderName) {
		File folder = new File(folderName);
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new RuntimeException(String.format("Unable to create folder(%s).", folderName));
			}
		}
	}

	private void createFileIfNeededAndMapToBuffer(String folderName, String fileName) {
		File file = new File(folderName, fileName);
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					throw new RuntimeException(String.format("Unable to create file(%s).", file.getAbsolutePath()));
				}
			} catch (IOException e) {
				throw new RuntimeException(String.format("Unable to create file(%s).", file.getAbsolutePath()));
			}
		}

		try {
			mbb = new RandomAccessFile(file, "rwd").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 200);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Unable to map file(%s) to buffer.", file.getAbsolutePath()));
		}
	}

	private void deleteFile(String folderName, String fileName) {
		File file = new File(folderName, fileName);
		if (!file.delete()) {
			throw new RuntimeException(String.format("Unable to delete file(%s).", file.getAbsolutePath()));
		}
	}

	protected void loadBreakpoint() {
		try {
			mbb.position(0);
			String[] breakpointInfos = Charset.defaultCharset().decode(mbb).toString().split("\n");
			oriSeq = Long.parseLong(breakpointInfos[0]);
			oriBinlogInfo = new BinlogInfo(breakpointInfos[1], Long.parseLong(breakpointInfos[2]), Integer.parseInt(breakpointInfos[3]));
		} catch (RuntimeException e) {
			String msg = String.format("Binlog manager(%s) load breakpoint error.", name);
			LOG.error(msg);
		}
	}

	protected void saveBreakpoint() {
		mbb.position(0);
		mbb.put(new byte[200]);
		mbb.position(0);
		mbb.put(String.valueOf(getSeq()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(getBinlogInfo().getBinlogFile()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(getBinlogInfo().getBinlogPosition()).getBytes());
		mbb.put("\n".getBytes());
		mbb.put(String.valueOf(getBinlogInfo().getEventIndex()).getBytes());
		mbb.put("\n".getBytes());
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
}
