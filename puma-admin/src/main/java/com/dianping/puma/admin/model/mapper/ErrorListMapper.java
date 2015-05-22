package com.dianping.puma.admin.model.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.puma.admin.config.Config.ErrorCode;
import com.dianping.puma.admin.model.ErrorCodeDto;
import com.dianping.puma.admin.model.ErrorDto;
import com.dianping.puma.admin.model.ErrorHandlerDto;
import com.dianping.puma.admin.model.ErrorListDto;
import com.dianping.puma.admin.model.ErrorSetDto;

public class ErrorListMapper {

	public static ErrorSetDto convertToErrorSetDto(Map<ErrorCode, List<String>> errors) {
		ErrorSetDto errorSetDto = new ErrorSetDto();
		List<ErrorCodeDto> errorCodeDtos = new ArrayList<ErrorCodeDto>();
		List<ErrorHandlerDto> errorHandlerDtos = new ArrayList<ErrorHandlerDto>();
		for (Map.Entry<ErrorCode, List<String>> error : errors.entrySet()) {
			ErrorCodeDto errorCodeDto = new ErrorCodeDto();
			errorCodeDto.setErrorCode(error.getKey().getErrorCode());
			errorCodeDto.setDesc(error.getKey().getDesc());
			errorCodeDtos.add(errorCodeDto);
			if (error.getValue() != null) {
				for (String errorValue : error.getValue()) {
					ErrorHandlerDto errorHandlerDto = new ErrorHandlerDto();
					errorHandlerDto.setErrorCode(error.getKey().getErrorCode());
					errorHandlerDto.setName(errorValue);
					errorHandlerDto.setDesc(errorValue);
					errorHandlerDtos.add(errorHandlerDto);
				}
				
			}
		}
		errorSetDto.setErrorCodes(errorCodeDtos);
		errorSetDto.setErrorHandlers(errorHandlerDtos);
		return errorSetDto;
	}

	public static ErrorListDto convertToErrorList(Map<Integer, String> errors) {
		ErrorListDto errorListDto = new ErrorListDto();
		if (errors != null) {
			List<ErrorDto> errorDtos = new ArrayList<ErrorDto>();
			for (Map.Entry<Integer, String> error : errors.entrySet()) {
				ErrorDto errorDto = new ErrorDto();
				ErrorCodeDto errorCodeDto = new ErrorCodeDto();
				ErrorHandlerDto errorHandlerDto = new ErrorHandlerDto();
				errorCodeDto.setErrorCode(error.getKey());
				errorHandlerDto.setName(error.getValue());
				errorHandlerDto.setDesc(error.getValue());
				errorDto.setErrorCode(errorCodeDto);
				errorDto.setSelectedHandler(errorHandlerDto);
				errorDtos.add(errorDto);
			}
			errorListDto.setErrors(errorDtos);
		}
		return errorListDto;
	}

	public static Map<Integer, String> convertToErrors(ErrorListDto errorListDto) {
		Map<Integer, String> errors = new HashMap<Integer, String>();
		if (errorListDto != null && errorListDto.getErrors() != null) {
			for (ErrorDto errorDto : errorListDto.getErrors()) {
				errors.put(errorDto.getErrorCode().getErrorCode(), errorDto.getSelectedHandler().getName());
			}
		}
		return errors;
	}
}
