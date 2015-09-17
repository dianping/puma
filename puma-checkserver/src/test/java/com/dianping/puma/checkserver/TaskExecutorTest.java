package com.dianping.puma.checkserver;

import com.dianping.puma.checkserver.comparison.Comparison;
import com.dianping.puma.checkserver.comparison.FullComparison;
import com.dianping.puma.checkserver.datasource.DataSourceBuilder;
import com.dianping.puma.checkserver.fetcher.SingleLineTargetFetcher;
import com.dianping.puma.checkserver.fetcher.UpdateTimeAndIdSourceFetcher;
import com.dianping.puma.checkserver.mapper.DefaultRowMapper;
import com.dianping.puma.checkserver.mapper.RowMapper;
import com.dianping.puma.checkserver.model.SourceTargetPair;
import com.dianping.puma.checkserver.model.TaskResult;
import com.google.common.collect.Sets;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutorTest {
    private final Date startTime = new Date(1388534400000l); //2014-01-01
    private final Date endTime = new Date(1391212800000l); //2014-02-01
    private final ComboPooledDataSource sourceDs = new ComboPooledDataSource();
    private final ComboPooledDataSource targetDs = new ComboPooledDataSource();
    private final JdbcTemplate sourceTemplate = new JdbcTemplate(sourceDs);
    private final JdbcTemplate targetTemplate = new JdbcTemplate(targetDs);

    private TaskExecutor target;

    @Before
    public void setUp() throws Exception {
        sourceDs.setJdbcUrl("jdbc:h2:mem:fulltest1");
        targetDs.setJdbcUrl("jdbc:h2:mem:fulltest2");
        initData(sourceTemplate);
        initData(targetTemplate);

        DataSourceBuilder sourceBuilder = mock(DataSourceBuilder.class);
        when(sourceBuilder.build()).thenReturn(sourceDs);

        DataSourceBuilder targetBuilder = mock(DataSourceBuilder.class);
        when(targetBuilder.build()).thenReturn(targetDs);

        UpdateTimeAndIdSourceFetcher sourceFetcher = new UpdateTimeAndIdSourceFetcher();
        sourceFetcher.setStartTime(startTime);
        sourceFetcher.setEndTime(endTime);
        sourceFetcher.setColumns("*");
        sourceFetcher.setTableName("Debug");

        SingleLineTargetFetcher targetFetcher = new SingleLineTargetFetcher();
        targetFetcher.setColumns("*");
        targetFetcher.setTableName("Debug");

        sourceFetcher.init(sourceDs);
        targetFetcher.init(targetDs);

        Comparison comparison = new FullComparison();
        RowMapper mapper = new DefaultRowMapper().setMapKey(Sets.newHashSet("ID"));

        target = TaskExecutor.Builder.create()
                .setSourceBuilder(sourceBuilder)
                .setTargetBuilder(targetBuilder)
                .setSourceFetcher(sourceFetcher)
                .setTargetFetcher(targetFetcher)
                .setComparison(comparison)
                .setRowMapper(mapper)
                .build();
    }

    @Test
    public void testSame() throws Exception {
        TaskResult result = target.call();
        Assert.assertEquals(0, result.getDifference().size());
    }

    @Test
    public void testNotEquals() throws Exception {
        targetTemplate.update("UPDATE Debug SET UpdateTime = now() where ID = ?", 1);

        List<SourceTargetPair> difference = new ArrayList<SourceTargetPair>();

        target.fullCompare(difference);
        Assert.assertEquals(1, difference.size());
    }

    @Test
    public void testRetrySuccess() throws Exception {
        sourceTemplate.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", 10000, startTime);

        List<SourceTargetPair> difference = new ArrayList<SourceTargetPair>();

        target.fullCompare(difference);
        Assert.assertEquals(1, difference.size());
        Assert.assertNull(difference.get(0).getTarget());

        targetTemplate.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", 10000, startTime);

        target.retry(difference);
        Assert.assertEquals(0, difference.size());
    }

    @Test
    public void testRetryFailed() throws Exception {
        sourceTemplate.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", 10000, startTime);

        List<SourceTargetPair> difference = new ArrayList<SourceTargetPair>();

        target.fullCompare(difference);
        Assert.assertEquals(1, difference.size());
        Assert.assertNull(difference.get(0).getTarget());

        target.retry(difference);
        Assert.assertEquals(1, difference.size());
        Assert.assertNull(difference.get(0).getTarget());
    }

    private void initData(JdbcTemplate template) {
        template.execute("DROP TABLE IF EXISTS Debug");
        template.execute(
                "CREATE TABLE Debug (\n" +
                        "  ID int,\n" +
                        "  UpdateTime timestamp,\n" +
                        "  PRIMARY KEY (Id))");

        for (int k = 1; k <= 3456; k++) {
            template.update("INSERT INTO Debug (ID,UpdateTime) VALUES (?,?)", k, new Date(startTime.getTime() + 1));
        }
    }
}