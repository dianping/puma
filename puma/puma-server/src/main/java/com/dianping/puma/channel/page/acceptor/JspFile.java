package com.dianping.puma.channel.page.acceptor;

public enum JspFile {
	VIEW("/jsp/channel/acceptor.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
