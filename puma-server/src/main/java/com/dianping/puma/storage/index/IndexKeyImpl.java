package com.dianping.puma.storage.index;

public class IndexKeyImpl implements IndexKey<IndexKeyImpl> {

	private long timestamp;

	private String binlogFile;

	private long binlogPosition;

	private long serverId;

	public IndexKeyImpl() {
	}
	
	public IndexKeyImpl(long timestamp) {
	   super();
	   this.timestamp = timestamp;
   }

	public IndexKeyImpl(long serverId, String binlogFile, long binlogPosition) {
		this(0L, serverId, binlogFile, binlogPosition);
	}

	public IndexKeyImpl(long timestamp, long serverId, String binlogFile, long binlogPosition) {
		this.timestamp = timestamp;
		this.serverId = serverId;
		this.binlogFile = binlogFile;
		this.binlogPosition = binlogPosition;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setBinlogFile(String binlogFile) {
		this.binlogFile = binlogFile;
	}

	public void setBinlogPosition(long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	@Override
	public String getBinlogFile() {
		return binlogFile;
	}

	@Override
	public long getServerId() {
		return serverId;
	}

	@Override
	public int compareTo(IndexKeyImpl o) {
		if (this.timestamp > o.timestamp) {
			return 1;
		} else if (this.timestamp == o.timestamp) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public long getBinlogPosition() {
		return binlogPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binlogFile == null) ? 0 : binlogFile.hashCode());
		result = prime * result + (int) (binlogPosition ^ (binlogPosition >>> 32));
		result = prime * result + (int) (serverId ^ (serverId >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IndexKeyImpl other = (IndexKeyImpl) obj;

		if (binlogFile == null) {
			if (other.binlogFile != null) {
				return false;
			}
		} else if (!binlogFile.equals(other.binlogFile)) {
			return false;
		}
		if (binlogPosition != other.binlogPosition) {
			return false;
		}
		if (serverId != other.serverId) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}

		return true;
	}
}
