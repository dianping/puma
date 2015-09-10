package com.dianping.puma.comparison.fetcher;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class SingleLineTargetFetcherTest {

    ComboPooledDataSource ds = new ComboPooledDataSource();

    SingleLineTargetFetcher target = new SingleLineTargetFetcher();

    @Before
    public void setUp() throws Exception {
        ds.setJdbcUrl("jdbc:h2:mem:test");
        initData();
    }

    private void initData() {
        JdbcTemplate template = new JdbcTemplate(ds);

        template.execute("DROP TABLE IF EXISTS Debug");
        template.execute(
                "CREATE TABLE Debug (\n" +
                        "  ID int,\n" +
                        "  UpdateTime timestamp,\n" +
                        "  PRIMARY KEY (Id))");

        for (int k = 1; k <= 8888; k++) {
            template.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", k, new Date());
        }
    }

    @Test
    public void testFetch() throws Exception {
        target.init(ds);
        target.setTableName("Debug");
        target.setColumns("ID,UpdateTime");

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        for (int k = 1; k <= 8888; k++) {
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("ID", k);
            result.add(target.fetch(source, source).getTarget());
        }

        Assert.assertEquals(8888, result.size());

        int index = 1;
        for (Map<String, Object> row : result) {
            long lastId = ((Number) row.get("ID")).longValue();
            Assert.assertEquals(index++, lastId);
        }
    }
}