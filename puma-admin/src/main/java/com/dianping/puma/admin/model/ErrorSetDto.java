package com.dianping.puma.admin.model;

import java.util.List;

public class ErrorSetDto {
	
	private List<ErrorCodeDto> errorCodes;
	
	private List<ErrorHandlerDto> errorHandlers;

	public List<ErrorCodeDto> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(List<ErrorCodeDto> errorCodes) {
		this.errorCodes = errorCodes;
	}

	public List<ErrorHandlerDto> getErrorHandlers() {
		return errorHandlers;
	}

	public void setErrorHandlers(List<ErrorHandlerDto> errorHandlers) {
		this.errorHandlers = errorHandlers;
	}

}
