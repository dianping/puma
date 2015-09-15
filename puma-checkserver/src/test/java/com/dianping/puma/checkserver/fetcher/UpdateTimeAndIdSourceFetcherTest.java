package com.dianping.puma.checkserver.fetcher;

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
public class UpdateTimeAndIdSourceFetcherTest {

    ComboPooledDataSource ds = new ComboPooledDataSource();

    Date startTime = new Date(1388534400000l); //2014-01-01

    Date endTime = new Date(1391212800000l); //2014-02-01

    UpdateTimeAndIdSourceFetcher target = new UpdateTimeAndIdSourceFetcher();

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
            template.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", k, new Date(startTime.getTime() + 1));
        }

        template.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", 8889, new Date());
    }

    @Test
    public void testFetch() throws Exception {
        target.setTableName("Debug");
        target.setStartTime(startTime);
        target.setEndTime(endTime);
        target.setColumns("Id,UpdateTime");
        target.init(ds);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        while (true) {
            List<Map<String, Object>> rows = target.fetch();
            if (rows.size() == 0) {
                break;
            }
            result.addAll(rows);
        }

        Assert.assertEquals(8888, result.size());

        int index = 1;
        for (Map<String, Object> row : result) {
            long lastId = ((Number) row.get("ID")).longValue();
            Assert.assertEquals(index++, lastId);
        }
    }

    @Test
    public void testRetry() throws Exception {
        target.setTableName("Debug");
        target.setStartTime(startTime);
        target.setEndTime(endTime);
        target.setColumns("Id,UpdateTime");
        target.init(ds);

        Map<String, Object> row = new HashMap<String, Object>();
        row.put("ID", 1);

        Map<String, Object> result = target.retry(row);

        Assert.assertEquals(1, result.get("ID"));
    }
}