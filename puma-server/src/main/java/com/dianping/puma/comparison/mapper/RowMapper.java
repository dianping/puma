package com.dianping.puma.comparison.mapper;

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
    List<Map<String, Object>> map(List<Map<String, Object>> source);
}