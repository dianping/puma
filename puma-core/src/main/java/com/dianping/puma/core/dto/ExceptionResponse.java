package com.dianping.puma.core.dto;

public class ExceptionResponse {

	private String errorMsg;

	public ExceptionResponse(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}
