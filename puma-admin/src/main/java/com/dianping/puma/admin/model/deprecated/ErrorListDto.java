package com.dianping.puma.admin.model.deprecated;

import java.util.List;

public class ErrorListDto {
	
	private List<ErrorDto> errors;
	
	public ErrorListDto(){
		
	}

	public List<ErrorDto> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorDto> errors) {
		this.errors = errors;
	}
	
}
