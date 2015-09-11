package com.dianping.puma.comparison.comparison;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class FullComparisonTest {
    private FullComparison comparison = new FullComparison();

    @Test
    public void test_same() throws Exception {
        Map<String, Object> source = new HashMap<String, Object>();
        Map<String, Object> target = new HashMap<String, Object>();

        source.put("1", 1);
        target.put("1", 1);

        source.put("2", "2");
        target.put("2", "2");

        source.put("3", new Date(1441787666114l));
        target.put("3", new Date(1441787666114l));

        source.put("4", new byte[]{1, 2, 3, 4, 5});
        target.put("4", new byte[]{1, 2, 3, 4, 5});

        Assert.assertTrue(comparison.compare(source, target));
    }

    @Test
    public void test_byte_array() throws Exception {
        Map<String, Object> source = new HashMap<String, Object>();
        Map<String, Object> target = new HashMap<String, Object>();

        source.put("1", new byte[]{1, 2, 3, 4, 6});
        target.put("1", new byte[]{1, 2, 3, 4, 5});

        Assert.assertFalse(comparison.compare(source, target));
    }
}