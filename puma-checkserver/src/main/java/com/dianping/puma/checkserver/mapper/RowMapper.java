package com.dianping.puma.checkserver.mapper;

import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 * <p/>
 * 用语映射原数据和目标数据
 */
public interface RowMapper {

    List<Map<String, Object>> mapToTarget(List<Map<String, Object>> source);

    Map<String, Object> mapToTarget(Map<String, Object> source);

    Map<String, Object> mapToSource(Map<String, Object> source);

}