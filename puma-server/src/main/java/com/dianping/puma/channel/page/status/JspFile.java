package com.dianping.puma.channel.page.status;

public enum JspFile {
	VIEW("/jsp/channel/status.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
