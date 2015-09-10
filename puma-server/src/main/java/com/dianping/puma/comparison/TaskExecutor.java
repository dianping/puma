package com.dianping.puma.comparison;

import com.dianping.cat.Cat;
import com.dianping.puma.comparison.comparison.Comparison;
import com.dianping.puma.comparison.datasource.DataSourceBuilder;
import com.dianping.puma.comparison.fetcher.SourceFetcher;
import com.dianping.puma.comparison.fetcher.TargetFetcher;
import com.dianping.puma.comparison.mapper.RowMapper;
import com.dianping.puma.comparison.model.SourceTargetPair;
import com.dianping.puma.core.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutor implements Callable<TaskResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

    private final TaskEntity task;

    private final DataSource sourceDataSource;

    private final DataSource targetDataSource;

    private final RowMapper rowMapper;

    private final SourceFetcher sourceFetcher;

    private final TargetFetcher targetFetcher;

    private final Comparison comparison;

    public TaskExecutor(TaskEntity task) {
        this.task = task;
        this.sourceDataSource = initSourceDataSource(task);
        this.targetDataSource = initTargetDataSource(task);
        this.sourceFetcher = initSourceFetcher(task);
        this.sourceFetcher.init(this.sourceDataSource);
        this.targetFetcher = initTargetFetcher(task);
        this.targetFetcher.init(this.targetDataSource);
        this.rowMapper = initRowMapper(task);
        this.comparison = initComparison(task);
    }

    protected Comparison initComparison(TaskEntity task) {
        return (Comparison) fromClassNameAndJson(task.getComparison(), task.getComparisonProp());
    }

    protected TargetFetcher initTargetFetcher(TaskEntity task) {
        return (TargetFetcher) fromClassNameAndJson(task.getTargetFetcher(), task.getTargetFetcherProp());
    }

    protected SourceFetcher initSourceFetcher(TaskEntity task) {
        return (SourceFetcher) fromClassNameAndJson(task.getSourceFetcher(), task.getSourceFetcherProp());
    }

    protected RowMapper initRowMapper(TaskEntity task) {
        return (RowMapper) fromClassNameAndJson(task.getMapper(), task.getMapperProp());
    }

    protected DataSource initTargetDataSource(TaskEntity task) {
        DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(task.getTargetDsBuilder(), task.getTargetDsBuilderProp());
        return builder.build();
    }

    protected DataSource initSourceDataSource(TaskEntity task) {
        DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(task.getSourceDsBuilder(), task.getSourceDsBuilderProp());
        return builder.build();
    }

    protected Object fromClassNameAndJson(String className, String json) {
        try {
            return GsonUtil.fromJson(json, Class.forName(className));
        } catch (ClassNotFoundException e) {
            Cat.logError(className, e);
            LOG.error(className, e);
            throw new RuntimeException(className, e);
        }
    }

    @Override
    public TaskResult call() throws Exception {
        List<SourceTargetPair> difference = new ArrayList<SourceTargetPair>();

        fullCompare(difference);

        int tryTimes = 0;
        while (tryTimes++ < 3 && difference.size() > 0) {
            retry(difference);
            Thread.sleep(10 * 1000);
        }

        return new TaskResult().setDifference(difference);
    }

    protected void retry(List<SourceTargetPair> difference) {
        Iterator<SourceTargetPair> iterable = difference.iterator();
        while (iterable.hasNext()) {
            SourceTargetPair pair = iterable.next();

            Map<String, Object> mappedColumn = rowMapper.mapToSource(pair.getSource());
            Map<String, Object> sourceData = sourceFetcher.retry(mappedColumn);

            Map<String, Object> mappedColumnTarget;
            if (sourceData == null) {
                mappedColumnTarget = rowMapper.mapToTarget(pair.getSource());
            } else {
                mappedColumnTarget = rowMapper.mapToTarget(sourceData);
            }

            Map<String, Object> targetData = targetFetcher.fetch(mappedColumnTarget);

            if (comparison.compare(sourceData, targetData)) {
                iterable.remove();
            }
        }
    }

    protected void fullCompare(List<SourceTargetPair> difference) {
        List<Map<String, Object>> sourceData;
        do {
            sourceData = sourceFetcher.fetch();
            List<Map<String, Object>> mappedColumn = rowMapper.mapToTarget(sourceData);
            List<Map<String, Object>> targetData = targetFetcher.fetch(mappedColumn);
            List<SourceTargetPair> pairs = targetFetcher.map(sourceData, targetData);

            for (SourceTargetPair pair : pairs) {
                if (!comparison.compare(pair.getSource(), pair.getTarget())) {
                    difference.add(pair);
                }
            }
        } while (sourceData != null && sourceData.size() > 0);
    }
}
