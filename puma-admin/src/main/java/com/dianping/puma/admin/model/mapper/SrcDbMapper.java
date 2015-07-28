package com.dianping.puma.admin.model.mapper;

import com.dianping.puma.admin.model.SrcDbDto;
import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SrcDbMapper {

	public static List<SrcDbDto> map(List<SrcDbEntity> srcDbEntities) {
		Map<String, SrcDbDto> srcDbDtoMap = new HashMap<String, SrcDbDto>();

		for (SrcDbEntity srcDbEntity: srcDbEntities) {
			SrcDbDto srcDbDto = srcDbDtoMap.get(srcDbEntity.getJdbcRef());

			if (srcDbDto == null) {
				srcDbDto = new SrcDbDto();
				srcDbDto.setMhaRef(srcDbEntity.getJdbcRef());
				srcDbDto.setUsername(srcDbEntity.getUsername());
				srcDbDto.setPassword(srcDbEntity.getPassword());
				srcDbDtoMap.put(srcDbDto.getMhaRef(), srcDbDto);
			}

			srcDbDto.getSrcDbEntities().add(srcDbEntity);
		}

		return new ArrayList<SrcDbDto>(srcDbDtoMap.values());
	}
}
