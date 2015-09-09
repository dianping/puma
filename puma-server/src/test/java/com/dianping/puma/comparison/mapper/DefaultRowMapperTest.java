package com.dianping.puma.comparison.mapper;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DefaultRowMapperTest {
    DefaultRowMapper target = new DefaultRowMapper();

    @Test
    public void testMap() throws Exception {
        target.setMapKey(Sets.newHashSet("ID"));

        List<Map<String, Object>> source = new ArrayList<Map<String, Object>>();
        Map<String, Object> row1 = new HashMap<String, Object>();
        row1.put("ID", 1);
        row1.put("Name", 1);

        Map<String, Object> row2 = new HashMap<String, Object>();
        row2.put("ID", 1);
        row2.put("Name", 1);

        source.add(row1);
        source.add(row2);

        List<Map<String, Object>> result = target.map(source);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(1, result.get(0).size());
        Assert.assertEquals(1, result.get(0).get("ID"));
        Assert.assertEquals(1, result.get(1).size());
        Assert.assertEquals(1, result.get(1).get("ID"));
    }
}