package com.dianping.puma.storage;

import com.dianping.puma.core.event.ChangedEvent;

public class DecoderElement {
	private boolean isDecoded;
	private String decodeErrorMsg;
	private byte[] data;
	private ChangedEvent changedEvent;

	public boolean isDecoded() {
		return isDecoded;
	}

	public void setDecoded(boolean isDecoded) {
		this.isDecoded = isDecoded;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ChangedEvent getChangedEvent() {
		return changedEvent;
	}

	public void setChangedEvent(ChangedEvent changedEvent) {
		this.changedEvent = changedEvent;
	}

	public String getDecodeErrorMsg() {
		return decodeErrorMsg;
	}

	public void setDecodeErrorMsg(String decodeErrorMsg) {
		this.decodeErrorMsg = decodeErrorMsg;
	}

}
