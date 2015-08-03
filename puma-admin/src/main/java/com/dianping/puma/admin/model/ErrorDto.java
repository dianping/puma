package com.dianping.puma.admin.model;

public class ErrorDto {
	
	private ErrorCodeDto errorCode;
	
	private ErrorHandlerDto selectedHandler;
	
	public ErrorCodeDto getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCodeDto errorCode) {
		this.errorCode = errorCode;
	}


	public ErrorHandlerDto getSelectedHandler() {
		return selectedHandler;
	}

	public void setSelectedHandler(ErrorHandlerDto selectedHandler) {
		this.selectedHandler = selectedHandler;
	}
}
